package hr.bill.spring_bill.service;

import hr.bill.spring_bill.clients.ais_eposlovanje.AisEposlovanjeClient;
import hr.bill.spring_bill.model.TenantEntity;
import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.BalancesResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.BalancesResponse.Balance;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.InstitutionResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.RequisitionResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.TransactionsResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.TransactionsResponse.Transaction;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.AisTransactionMapper;
import hr.bill.spring_bill.mapper.BankStatementMapper;
import hr.bill.spring_bill.mapper.CamtStatementMapper;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.service.document.BillStrategyFactory;
import hr.bill.spring_bill.service.document.RemoteMatchResult;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankStatementService {

    private static final List<String> BOOKED_BALANCE_TYPES = List.of("closingBooked", "interimBooked");

    private static final String CASH_WITHDRAWAL_PREFIX = "ISPLATA BANKOMAT";

    private static final String POS_PURCHASE_PREFIX = "POS KUPOVINA";

    private final AisEposlovanjeClient aisEposlovanjeClient;

    private final TenantService tenantService;

    private final AisTransactionMapper aisTransactionMapper;

    private final CamtXmlService camtXmlService;

    private final CamtStatementMapper camtStatementMapper;

    private final BankStatementRepository bankStatementRepository;

    private final BankTransactionRepository bankTransactionRepository;

    private final BillStrategyFactory billStrategyFactory;

    private final PosTransactionService posTransactionService;

    private final CashWithdrawalBalanceService cashWithdrawalBalanceService;

    private final BankStatementMapper bankStatementMapper;

    private final DocumentService documentService;

    private final MonthlySummaryScheduler monthlySummaryScheduler;

    @Value("${bill.statement.remote-match-lookback-months}")
    private int remoteMatchLookbackMonths;

    public List<BankStatementResponse> findAll() {
        log.debug("Fetching all bank statements");
        List<BankStatementResponse> result =
                bankStatementMapper.toBankStatementResponseList(bankStatementRepository.findAllByOrderByCreatedAtDesc());
        log.debug("Found {} bank statements", result.size());
        return result;
    }

    public BankStatementResponse importStatement(MultipartFile file) {
        log.info("Importing bank statement file '{}'", file.getOriginalFilename());
        return importStatementXml(readXml(file));
    }

    public List<BankStatementResponse> importStatements(List<MultipartFile> files, String email) {
        log.info("Importing {} bank statement file(s)", files.size());
        List<BankStatementResponse> imported = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".zip")) {
                imported.addAll(importStatementsZip(file));
            } else {
                BankStatementResponse response = importStatement(file);
                if (response != null) {
                    imported.add(response);
                }
            }
        }
        log.info("Imported {} bank statement(s) from {} file(s)", imported.size(), files.size());
        monthlySummaryScheduler.refreshRecentMonths();
        if (email != null && !email.isBlank() && !imported.isEmpty()) {
            sendImportedStatements(imported, email.trim());
        }
        return imported;
    }

    public void sendImportedStatements(List<BankStatementResponse> imported, String email) {
        log.info("Sending {} imported bank statement(s) as PDF to {}", imported.size(), email);
        List<SendBillReportItem> reports = imported.stream()
                .map(statement -> SendBillReportItem.builder()
                        .id(statement.id().toString())
                        .billId(statement.statementId())
                        .type(BillReportType.BANK_STATEMENT)
                        .build())
                .toList();
        documentService.generateAndSendDocuments(SendBillReportsRequest.builder()
                .reports(reports)
                .email(email)
                .build());
    }

    public List<BankStatementResponse> importStatementsZip(MultipartFile zipFile) {
        log.info("Importing bank statements from zip file '{}'", zipFile.getOriginalFilename());
        List<BankStatementResponse> imported = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
                    log.debug("Importing zip entry '{}'", entry.getName());
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    BankStatementResponse response = importStatementXml(xml);
                    if (response != null) {
                        imported.add(response);
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        log.info("Imported {} bank statement(s) from zip file '{}'", imported.size(), zipFile.getOriginalFilename());
        return imported;
    }


    public List<BankStatementResponse> getTransactions(LocalDate dateFrom, LocalDate dateTill) {
        log.info("Importing bank statements from AIS for {} - {}", dateFrom, dateTill);
        if (dateFrom.isAfter(dateTill)) {
            throw new IllegalArgumentException("dateFrom must not be after dateTill");
        }
        if (!dateTill.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("dateTill must be before today, the current day is not closed yet");
        }

        RequisitionResponse requisition = aisEposlovanjeClient.getRequisitions().results().stream()
                .filter(r -> "LN".equals(r.status()) && r.accounts() != null && !r.accounts().isEmpty())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No linked AIS requisition with an account found"));
        String accountId = requisition.accounts().getFirst();

        TransactionsResponse.Transactions fetched = aisEposlovanjeClient.getAccountTransactions(
                accountId, dateFrom.toString(), LocalDate.now().toString()).transactions();
        List<Transaction> booked = fetched != null && fetched.booked() != null ? fetched.booked() : List.of();
        log.debug("Fetched {} booked transaction(s) for account {}", booked.size(), accountId);

        Map<LocalDate, List<Transaction>> byDay = booked.stream()
                .filter(tx -> {
                    LocalDate day = aisTransactionMapper.bookingDate(tx);
                    return !day.isBefore(dateFrom) && !day.isAfter(dateTill);
                })
                .collect(Collectors.groupingBy(aisTransactionMapper::bookingDate, TreeMap::new, Collectors.toList()));
        byDay.keySet().removeIf(day -> {
            boolean exists = bankStatementRepository.existsByStatementId(statementId(day));
            if (exists) {
                log.info("Bank statement for {} already exists, skipping import", day);
            }
            return exists;
        });
        if (byDay.isEmpty()) {
            log.info("No new bank statement days to import for {} - {}", dateFrom, dateTill);
            return List.of();
        }

        Balance balance = bookedBalance(aisEposlovanjeClient.getAccountBalances(accountId));
        InstitutionResponse institution = aisEposlovanjeClient.getInstitution(requisition.institutionId());

        List<BankStatementResponse> imported = new ArrayList<>();
        byDay.forEach((day, dayTransactions) -> {
            BankStatementEntity statement = bankStatementRepository.save(
                    toBankStatementEntity(day, dayTransactions, institution, closingBalance(balance, day, booked)));
            List<BankTransactionEntity> transactions = dayTransactions.stream()
                    .map(tx -> aisTransactionMapper.toBankTransactionEntity(tx, statement.getId(), transactionType(tx)))
                    .toList();
            processTransactions(statement, transactions);
            imported.add(bankStatementMapper.toBankStatementResponse(statement));
        });

        monthlySummaryScheduler.refreshRecentMonths();
        log.info("Imported {} bank statement(s) from AIS for {} - {}", imported.size(), dateFrom, dateTill);
        return imported;
    }

    private BankStatementEntity toBankStatementEntity(LocalDate day, List<Transaction> transactions,
                                                      InstitutionResponse institution, BigDecimal closingBalance) {
        TenantEntity tenant = tenantService.get();
        List<BigDecimal> amounts = transactions.stream().map(aisTransactionMapper::signedAmount).toList();
        List<BigDecimal> credits = amounts.stream().filter(a -> a.signum() > 0).toList();
        List<BigDecimal> debits = amounts.stream().filter(a -> a.signum() < 0).map(BigDecimal::abs).toList();
        BigDecimal net = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return BankStatementEntity.builder()
                .statementId(statementId(day))
                .accountName("")
                .bankBic(institution.bic())
                .createdAt(LocalDateTime.now())
                .currency(transactions.getFirst().transactionAmount().currency())
                .iban(tenant.getIban())
                .ownerAddress(String.format("%s, %s", tenant.getCity().toUpperCase(),
                        tenant.getStreet().toUpperCase()))
                .ownerName(tenant.getName())
                .ownerOib(tenant.getOib())
                .periodFrom(day)
                .periodTo(day)
                .openingBalance(closingBalance != null ? closingBalance.subtract(net) : null)
                .closingBalance(closingBalance)
                .creditCount(credits.size())
                .creditSum(credits.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .debitCount(debits.size())
                .debitSum(debits.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

    private BankTransactionType transactionType(Transaction tx) {
        if (aisTransactionMapper.iban(tx.creditorAccount()) != null || tx.remittanceInformationUnstructured() == null) {
            return BankTransactionType.TRANSACTION;
        }
        String text = tx.remittanceInformationUnstructured().trim().toUpperCase();
        if (text.startsWith(CASH_WITHDRAWAL_PREFIX)) {
            return BankTransactionType.BANK_WITHDRAWAL;
        }
        if (text.startsWith(POS_PURCHASE_PREFIX)) {
            return BankTransactionType.POS_PAY;
        }
        return BankTransactionType.TRANSACTION;
    }

    private String statementId(LocalDate day) {
        TenantEntity tenant = tenantService.get();
        return tenant.getIban() + "-" + day.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private Balance bookedBalance(BalancesResponse response) {
        if (response == null || response.balances() == null) {
            return null;
        }
        for (String type : BOOKED_BALANCE_TYPES) {
            Optional<Balance> balance = response.balances().stream()
                    .filter(b -> type.equals(b.balanceType()) && b.balanceAmount() != null)
                    .findFirst();
            if (balance.isPresent()) {
                return balance.get();
            }
        }
        log.warn("AIS returned no booked balance, statements will be saved without opening/closing balance");
        return null;
    }

    private BigDecimal closingBalance(Balance balance, LocalDate day, List<Transaction> booked) {
        if (balance == null) {
            return null;
        }
        LocalDate referenceDate = balance.referenceDate() != null ? LocalDate.parse(balance.referenceDate()) : LocalDate.now();
        BigDecimal result = new BigDecimal(balance.balanceAmount().amount().trim());
        for (Transaction tx : booked) {
            LocalDate bookingDate = aisTransactionMapper.bookingDate(tx);
            if (bookingDate.isAfter(day) && !bookingDate.isAfter(referenceDate)) {
                result = result.subtract(aisTransactionMapper.signedAmount(tx));
            } else if (bookingDate.isAfter(referenceDate) && !bookingDate.isAfter(day)) {
                result = result.add(aisTransactionMapper.signedAmount(tx));
            }
        }
        return result;
    }

    private BankStatementResponse importStatementXml(String xml) {
        CamtDocument document = camtXmlService.parse(xml);

        String statementId = document.getBkToCstmrStmt().getStmt().getId();
        if (bankStatementRepository.existsByStatementId(statementId)) {
            log.info("Bank statement '{}' already exists, skipping import", statementId);
            return null;
        }

        BankStatementEntity statement = bankStatementRepository.save(camtStatementMapper.toBankStatementEntity(document));
        processTransactions(statement, camtStatementMapper.toBankTransactionEntities(document, statement.getId()));

        return bankStatementMapper.toBankStatementResponse(statement);
    }

    private void processTransactions(BankStatementEntity statement, List<BankTransactionEntity> transactions) {
        transactions = bankTransactionRepository.saveAll(transactions);
        posTransactionService.importPosStatements(transactions.stream().filter(bt ->
                bt.getTransactionType().equals(BankTransactionType.POS_PAY)).toList());
        cashWithdrawalBalanceService.importWithdrawalStatements(transactions.stream().filter(bt ->
                bt.getTransactionType().equals(BankTransactionType.BANK_WITHDRAWAL)).toList());
        log.debug("Saved bank statement {} with {} transaction(s)", statement.getId(), transactions.size());

        int matched = billStrategyFactory.markPaidFromBankStatement(transactions);

        List<BankTransactionEntity> unresolved = transactions.stream()
                .filter(tx -> tx.getBillSystemId() == null)
                .toList();
        LocalDate matchFrom = statement.getPeriodFrom() == null
                ? null
                : statement.getPeriodFrom().minusMonths(remoteMatchLookbackMonths);
        RemoteMatchResult remoteMatchResult = billStrategyFactory.matchBillSystemIdsFromRemote(unresolved, matchFrom, statement.getPeriodTo());
        int remoteMatched = remoteMatchResult.matchedCount();

        if (matched > 0 || remoteMatched > 0) {
            bankTransactionRepository.saveAll(transactions);
        }
        if (!remoteMatchResult.updatedMonths().isEmpty()) {
            monthlySummaryScheduler.refreshMonths(remoteMatchResult.updatedMonths());
        }
        log.info("Bank statement {} matched {} bill(s) locally and resolved {} more transaction(s) from upstream",
                statement.getId(), matched, remoteMatched);
    }

    private String readXml(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
