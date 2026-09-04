package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankStatementImportService {

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

    private void sendImportedStatements(List<BankStatementResponse> imported, String email) {
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

    private BankStatementResponse importStatementXml(String xml) {
        CamtDocument document = camtXmlService.parse(xml);

        String statementId = document.getBkToCstmrStmt().getStmt().getId();
        if (bankStatementRepository.existsByStatementId(statementId)) {
            log.info("Bank statement '{}' already exists, skipping import", statementId);
            return null;
        }

        BankStatementEntity statement = bankStatementRepository.save(camtStatementMapper.toBankStatementEntity(document));

        List<BankTransactionEntity> transactions = camtStatementMapper.toBankTransactionEntities(document, statement.getId());
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

        return bankStatementMapper.toBankStatementResponse(statement);
    }

    private String readXml(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
