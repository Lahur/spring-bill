package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.AccountsStatementRepository;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementResponse;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CreateAccountsStatementRequest;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.AccountsStatementMapper;
import hr.bill.spring_bill.model.AccountsStatementEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountsStatementService {

    private final AccountsStatementRepository accountsStatementRepository;

    private final CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    private final AccountsStatementMapper accountsStatementMapper;

    @Value("${bill.path.accounts-statement}")
    private String accountsStatementPath;

    public List<AccountsStatementResponse> findAll() {
        log.debug("Fetching all accounts statements");
        List<AccountsStatementEntity> result = accountsStatementRepository.findAll();
        log.debug("Found {} accounts statements", result.size());
        return accountsStatementMapper.toAccountsStatementResponseList(result);
    }

    public AccountsStatementEntity findById(UUID id) {
        log.debug("Fetching accounts statement {}", id);
        return accountsStatementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Accounts statement not found for id: " + id));
    }

    public AccountsStatementResponse create(CreateAccountsStatementRequest request, MultipartFile file) {
        log.info("Creating accounts statement");
        if (file != null && !file.isEmpty() && !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Uploaded file must be a PDF");
        }
        String billPath = file != null && !file.isEmpty() ? storeFile(file).toString() : null;
        AccountsStatementEntity entity = accountsStatementMapper.toAccountsStatementEntity(request, billPath);
        AccountsStatementEntity saved = accountsStatementRepository.save(entity);
        log.info("Created accounts statement {}", saved.getId());
        return accountsStatementMapper.toAccountsStatementResponse(saved);
    }

    public AccountsStatementResponse upload(UUID id, MultipartFile file) {
        log.info("Uploading PDF for accounts statement {}", id);
        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Uploaded file must be a non-empty PDF");
        }
        AccountsStatementEntity entity = findById(id);
        entity.setBillPath(storeFile(file, id).toString());
        AccountsStatementEntity saved = accountsStatementRepository.save(entity);
        log.info("Uploaded PDF for accounts statement {}", id);
        return accountsStatementMapper.toAccountsStatementResponse(saved);
    }

    public List<AccountsStatementResponse> syncWithCashWithdrawals(List<UUID> accountsStatementIds) {
        log.info("Syncing accounts statements with cash withdrawal balances");
        List<AccountsStatementEntity> entities = accountsStatementRepository.findAllByIdInOrderByDateDesc(accountsStatementIds);
        List<CashWithdrawalBalanceEntity> cashWithdrawalBalanceEntities = cashWithdrawalBalanceRepository
                .findAllByBalanceGreaterThanOrderByBankTransaction_TransactionDate(BigDecimal.ZERO);

        Set<CashWithdrawalBalanceEntity> updatedCashWithdrawalBalances = new LinkedHashSet<>();
        Iterator<CashWithdrawalBalanceEntity> availableBalances = cashWithdrawalBalanceEntities.iterator();
        CashWithdrawalBalanceEntity current = availableBalances.hasNext() ? availableBalances.next() : null;

        for (AccountsStatementEntity entity : entities) {
            BigDecimal remaining = entity.getAmount();
            Set<CashWithdrawalBalanceEntity> usedCashWithdrawalBalances = new LinkedHashSet<>(entity.getCashWithdrawalBalances());
            while (remaining.compareTo(BigDecimal.ZERO) > 0 && current != null) {
                BigDecimal deduction = remaining.compareTo(current.getBalance()) <= 0
                        ? remaining
                        : current.getBalance();
                current.setBalance(current.getBalance().subtract(deduction));
                remaining = remaining.subtract(deduction);
                usedCashWithdrawalBalances.add(current);
                updatedCashWithdrawalBalances.add(current);
                if (current.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                    current = availableBalances.hasNext() ? availableBalances.next() : null;
                }
            }
            if (remaining.compareTo(BigDecimal.ZERO) > 0 && current == null) {
                throw new IllegalArgumentException(
                        "Ran out of cash withdrawal balance while syncing accounts statement " + entity.getId());
            }
            entity.setCashWithdrawalBalances(usedCashWithdrawalBalances);
            usedCashWithdrawalBalances.forEach(balance -> balance.getAccountsStatements().add(entity));
        }

        cashWithdrawalBalanceRepository.saveAll(updatedCashWithdrawalBalances);
        List<AccountsStatementEntity> saved = accountsStatementRepository.saveAll(entities);
        log.info("Synced {} accounts statements with cash withdrawal balances", saved.size());
        return accountsStatementMapper.toAccountsStatementResponseList(saved);
    }

    private Path storeFile(MultipartFile file) {
        return storeFile(file, UUID.randomUUID());
    }

    private Path storeFile(MultipartFile file, UUID id) {
        try {
            Path dir = Path.of(accountsStatementPath);
            Files.createDirectories(dir);
            Path target = dir.resolve(id + ".pdf");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}