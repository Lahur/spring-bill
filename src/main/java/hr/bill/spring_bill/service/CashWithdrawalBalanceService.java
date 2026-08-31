package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.AccountsStatementRepository;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dao.TenantPropertyRepository;
import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementRequest;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CashWithdrawalBalanceResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.AccountsStatementMapper;
import hr.bill.spring_bill.mapper.CashWithdrawalBalanceMapper;
import hr.bill.spring_bill.model.AccountsStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import hr.bill.spring_bill.model.TenantPropertyEntity;
import hr.bill.spring_bill.model.enums.TenantPropety;
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
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CashWithdrawalBalanceService {

    private final CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    private final AccountsStatementRepository accountsStatementRepository;

    private final CashWithdrawalBalanceMapper cashWithdrawalBalanceMapper;

    private final AccountsStatementMapper accountsStatementMapper;

    private final TenantPropertyRepository tenantPropertyRepository;

    @Value("${bill.path.accounts-statement}")
    private String accountsStatementPath;

    public List<CashWithdrawalBalanceResponse> findAll() {
        log.debug("Fetching all cash withdrawal balances");
        List<CashWithdrawalBalanceEntity> result = cashWithdrawalBalanceRepository.findAll();
        log.debug("Found {} cash withdrawal balances", result.size());
        return cashWithdrawalBalanceMapper.toCashWithdrawalBalanceResponseList(result);
    }

    public void importWithdrawalStatements(List<BankTransactionEntity> bankTransactions) {
        Map<TenantPropety, TenantPropertyEntity> counters = loadCounters();
        List<CashWithdrawalBalanceEntity> cashWithdrawalBalances = bankTransactions.stream().map(bt -> CashWithdrawalBalanceEntity.builder()
                .total(bt.getAmount())
                .balance(bt.getAmount())
                .disbursementNumber(increment(counters, TenantPropety.DISBURSEMENT_COUNT))
                .depositNumber(increment(counters, TenantPropety.DEPOSIT_COUNT))
                .bankTransaction(BankTransactionEntity.builder().id(bt.getId()).build())
                .build()).toList();
        cashWithdrawalBalanceRepository.saveAll(cashWithdrawalBalances);
    }

    private Map<TenantPropety, TenantPropertyEntity> loadCounters() {
        Map<TenantPropety, TenantPropertyEntity> counters = new EnumMap<>(TenantPropety.class);
        tenantPropertyRepository.findByPropertyIn(List.of(TenantPropety.DEPOSIT_COUNT, TenantPropety.DISBURSEMENT_COUNT))
                .forEach(entity -> counters.put(entity.getProperty(), entity));
        return counters;
    }

    private int increment(Map<TenantPropety, TenantPropertyEntity> counters, TenantPropety property) {
        TenantPropertyEntity entity = counters.computeIfAbsent(property,
                p -> TenantPropertyEntity.builder().property(p).value("0").build());
        int next = Integer.parseInt(entity.getValue()) + 1;
        entity.setValue(String.valueOf(next));
        tenantPropertyRepository.save(entity);
        return next;
    }

    public List<CashWithdrawalBalanceResponse> createAccountsStatement(AccountsStatementRequest request, MultipartFile file) {
        log.info("Creating accounts statement");
        if (file != null && !file.isEmpty() && !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Uploaded file must be a PDF");
        }

        Set<CashWithdrawalBalanceEntity> usedCashWithdrawalBalances = deductFromCashWithdrawalBalances(request);

        String billPath = file != null && !file.isEmpty() ? storeFile(file).toString() : null;
        AccountsStatementEntity entity = accountsStatementMapper.toAccountsStatementEntity(request, billPath);
        entity.setCashWithdrawalBalances(usedCashWithdrawalBalances);
        AccountsStatementEntity saved = accountsStatementRepository.save(entity);

        usedCashWithdrawalBalances.forEach(balance -> balance.getAccountsStatements().add(saved));
        cashWithdrawalBalanceRepository.saveAll(usedCashWithdrawalBalances);

        log.info("Created accounts statement {}", saved.getId());
        return cashWithdrawalBalanceMapper.toCashWithdrawalBalanceResponseList(List.copyOf(usedCashWithdrawalBalances));
    }

    private Set<CashWithdrawalBalanceEntity> deductFromCashWithdrawalBalances(AccountsStatementRequest request) {
        BigDecimal remaining = request.amount();
        Set<CashWithdrawalBalanceEntity> usedCashWithdrawalBalances = new LinkedHashSet<>();
        for (Integer key : request.cashWithdrawalIds().keySet().stream().sorted().toList()) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            UUID cashWithdrawalId = request.cashWithdrawalIds().get(key);
            CashWithdrawalBalanceEntity cashWithdrawalBalance = cashWithdrawalBalanceRepository.findById(cashWithdrawalId)
                    .orElseThrow(() -> new NotFoundException("Cash withdrawal balance not found for id: " + cashWithdrawalId));

            BigDecimal deduction = remaining.compareTo(cashWithdrawalBalance.getBalance()) <= 0
                    ? remaining
                    : cashWithdrawalBalance.getBalance();
            cashWithdrawalBalance.setBalance(cashWithdrawalBalance.getBalance().subtract(deduction));
            remaining = remaining.subtract(deduction);
            usedCashWithdrawalBalances.add(cashWithdrawalBalance);
        }
        cashWithdrawalBalanceRepository.saveAll(usedCashWithdrawalBalances);
        return usedCashWithdrawalBalances;
    }

    private Path storeFile(MultipartFile file) {
        try {
            Path dir = Path.of(accountsStatementPath);
            Files.createDirectories(dir);
            Path target = dir.resolve(UUID.randomUUID() + ".pdf");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}