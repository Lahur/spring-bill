package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import hr.bill.spring_bill.xml.camt.model.CamtEntry;
import hr.bill.spring_bill.xml.camt.model.CamtEntryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {CreditDebitIndicator.class, LocalDateTime.class, BigDecimal.class})
public interface CamtStatementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "doc.bkToCstmrStmt.stmt.id", target = "statementId")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.id.iban", target = "iban")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.ccy", target = "currency")
    @Mapping(target = "periodFrom", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getFrToDt().getFrDtTm()).toLocalDate())")
    @Mapping(target = "periodTo", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getFrToDt().getToDtTm()).toLocalDate())")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getCreDtTm()))")
    BankStatementEntity toBankStatementEntity(CamtDocument doc);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "bankStatementId", target = "bankStatementId")
    @Mapping(target = "amount", expression = "java(new BigDecimal(entry.getAmt().getValue().trim()))")
    @Mapping(target = "creditDebitIndicator", expression = "java(CreditDebitIndicator.valueOf(entry.getCdtDbtInd()))")
    @Mapping(target = "senderIban", expression = "java(senderIban(entry, tx, statementIban))")
    @Mapping(target = "receiverIban", expression = "java(receiverIban(entry, tx, statementIban))")
    @Mapping(source = "tx.rmtInf.strd.cdtrRefInf.ref", target = "reference")
    @Mapping(source = "tx.rmtInf.strd.addtlRmtInf", target = "additionalRemittanceInfo")
    @Mapping(target = "transactionDate", expression = "java(entry.getBookgDt() != null ? LocalDateTime.parse(entry.getBookgDt().getDtTm()) : null)")
    BankTransactionEntity toBankTransactionEntity(CamtEntry entry, CamtEntryTransaction tx, UUID bankStatementId, String statementIban);

    default List<BankTransactionEntity> toBankTransactionEntities(CamtDocument doc, UUID bankStatementId) {
        var stmt = doc.getBkToCstmrStmt().getStmt();
        if (stmt.getNtry() == null) return List.of();
        String statementIban = stmt.getAcct().getId().getIban();
        return stmt.getNtry().stream()
                .filter(entry -> entry.getNtryDtls() != null && entry.getNtryDtls().getTxDtls() != null)
                .flatMap(entry -> entry.getNtryDtls().getTxDtls().stream()
                        .map(tx -> toBankTransactionEntity(entry, tx, bankStatementId, statementIban)))
                .toList();
    }

    default String senderIban(CamtEntry entry, CamtEntryTransaction tx, String statementIban) {
        if (CreditDebitIndicator.DBIT.name().equals(entry.getCdtDbtInd())) {
            return statementIban;
        }
        return tx.getRltdPties() != null && tx.getRltdPties().getDbtrAcct() != null
                ? tx.getRltdPties().getDbtrAcct().getId().getIban() : null;
    }

    default String receiverIban(CamtEntry entry, CamtEntryTransaction tx, String statementIban) {
        if (CreditDebitIndicator.DBIT.name().equals(entry.getCdtDbtInd())) {
            return tx.getRltdPties() != null && tx.getRltdPties().getCdtrAcct() != null
                    ? tx.getRltdPties().getCdtrAcct().getId().getIban() : null;
        }
        return statementIban;
    }
}
