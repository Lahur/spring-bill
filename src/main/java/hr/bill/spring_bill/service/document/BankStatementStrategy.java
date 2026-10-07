package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.BankStatementReportMapper;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankStatementStrategy implements DocumentStrategy {

    private final BankStatementRepository bankStatementRepository;

    private final BankTransactionRepository bankTransactionRepository;

    private final BankStatementReportMapper bankStatementReportMapper;

    private final BillPdfClient billPdfClient;

    @Override
    public BillReportType getType() {
        return BillReportType.BANK_STATEMENT;
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for bank statement {}", id);
        UUID statementId = UUID.fromString(id);
        BankStatementEntity statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new NotFoundException("Bank statement not found for id: " + id));
        List<BankTransactionEntity> transactions =
                bankTransactionRepository.findAllByBankStatement_IdOrderByTransactionDateAsc(statementId);

        byte[] content = billPdfClient.renderBankStatement(
                bankStatementReportMapper.toBankStatementRequest(statement, transactions));

        String filename = "bank-statement-" + (statement.getSequenceNumber() != null
                ? statement.getSequenceNumber()
                : statement.getId());
        return BillDocument.builder()
                .content(content)
                .filename(filename)
                .build();
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for bank statement {}", id);
        BankStatementEntity statement = bankStatementRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Bank statement not found for id: " + id));
        statement.setSentCount(statement.getSentCount() + 1);
        bankStatementRepository.save(statement);
    }
}
