package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.xml.camt.model.CamtBalance;
import hr.bill.spring_bill.xml.camt.model.CamtBankTransactionCodeFamily;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import hr.bill.spring_bill.xml.camt.model.CamtEntry;
import hr.bill.spring_bill.xml.camt.model.CamtEntryTransaction;
import hr.bill.spring_bill.xml.camt.model.CamtNumberAndSumOfTransactions;
import hr.bill.spring_bill.xml.camt.model.CamtParty;
import hr.bill.spring_bill.xml.camt.model.CamtPostalAddress;
import hr.bill.spring_bill.xml.camt.model.CamtStatement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", imports = {CreditDebitIndicator.class, BankTransactionType.class, BankStatementEntity.class, LocalDate.class, LocalDateTime.class, BigDecimal.class})
public interface CamtStatementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sentCount", ignore = true)
    @Mapping(source = "doc.bkToCstmrStmt.stmt.id", target = "statementId")
    @Mapping(target = "sequenceNumber", expression = "java(parseInteger(doc.getBkToCstmrStmt().getStmt().getLglSeqNb()))")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.id.iban", target = "iban")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.ccy", target = "currency")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.nm", target = "accountName")
    @Mapping(source = "doc.bkToCstmrStmt.stmt.acct.ownr.nm", target = "ownerName")
    @Mapping(target = "ownerAddress", expression = "java(ownerAddress(doc.getBkToCstmrStmt().getStmt()))")
    @Mapping(target = "ownerOib", expression = "java(ownerOib(doc.getBkToCstmrStmt().getStmt()))")
    @Mapping(target = "bankBic", expression = "java(reportingBic(doc.getBkToCstmrStmt().getStmt()))")
    @Mapping(target = "bankOib", expression = "java(reportingOib(doc.getBkToCstmrStmt().getStmt()))")
    @Mapping(target = "periodFrom", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getFrToDt().getFrDtTm()).toLocalDate())")
    @Mapping(target = "periodTo", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getFrToDt().getToDtTm()).toLocalDate())")
    @Mapping(target = "openingBalance", expression = "java(balance(doc.getBkToCstmrStmt().getStmt(), \"OPBD\"))")
    @Mapping(target = "closingBalance", expression = "java(balance(doc.getBkToCstmrStmt().getStmt(), \"CLBD\"))")
    @Mapping(target = "creditCount", expression = "java(summaryCount(doc.getBkToCstmrStmt().getStmt(), true))")
    @Mapping(target = "creditSum", expression = "java(summarySum(doc.getBkToCstmrStmt().getStmt(), true))")
    @Mapping(target = "debitCount", expression = "java(summaryCount(doc.getBkToCstmrStmt().getStmt(), false))")
    @Mapping(target = "debitSum", expression = "java(summarySum(doc.getBkToCstmrStmt().getStmt(), false))")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.parse(doc.getBkToCstmrStmt().getStmt().getCreDtTm()))")
    BankStatementEntity toBankStatementEntity(CamtDocument doc);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bankStatement", expression = "java(BankStatementEntity.builder().id(bankStatementId).build())")
    @Mapping(target = "amount", expression = "java(new BigDecimal(entry.getAmt().getValue().trim()))")
    @Mapping(target = "creditDebitIndicator", expression = "java(CreditDebitIndicator.valueOf(entry.getCdtDbtInd()))")
    @Mapping(target = "senderIban", expression = "java(senderIban(entry, tx, statementIban))")
    @Mapping(target = "receiverIban", expression = "java(receiverIban(entry, tx, statementIban))")
    @Mapping(target = "counterpartyName", expression = "java(counterpartyName(entry, tx))")
    @Mapping(target = "counterpartyAddress", expression = "java(counterpartyAddress(entry, tx))")
    @Mapping(source = "tx.rmtInf.strd.cdtrRefInf.ref", target = "reference")
    @Mapping(source = "tx.refs.endToEndId", target = "payerReference")
    @Mapping(source = "entry.acctSvcrRef", target = "entryReference")
    @Mapping(source = "tx.refs.acctSvcrRef", target = "transactionReference")
    @Mapping(source = "tx.rmtInf.strd.addtlRmtInf", target = "additionalRemittanceInfo")
    @Mapping(target = "transactionDate", expression = "java(entry.getBookgDt() != null ? LocalDateTime.parse(entry.getBookgDt().getDtTm()) : null)")
    @Mapping(target = "valueDate", expression = "java(entry.getValDt() != null && entry.getValDt().getDt() != null ? LocalDate.parse(entry.getValDt().getDt()) : null)")
    @Mapping(target = "transactionType", expression = "java(transactionType(entry))")
    @Mapping(target = "billSystemId", ignore = true)
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

    /**
     * The counterparty is the "other side" of the entry: for an outgoing payment it is the
     * creditor, for an incoming one it is the debtor.
     */
    default CamtParty counterparty(CamtEntry entry, CamtEntryTransaction tx) {
        if (tx.getRltdPties() == null) {
            return null;
        }
        boolean outgoing = CreditDebitIndicator.DBIT.name().equals(entry.getCdtDbtInd());
        var wrapper = outgoing ? tx.getRltdPties().getCdtr() : tx.getRltdPties().getDbtr();
        return wrapper != null ? wrapper.getPty() : null;
    }

    default String counterpartyName(CamtEntry entry, CamtEntryTransaction tx) {
        CamtParty party = counterparty(entry, tx);
        return party != null ? party.getNm() : null;
    }

    default String counterpartyAddress(CamtEntry entry, CamtEntryTransaction tx) {
        CamtParty party = counterparty(entry, tx);
        if (party == null || party.getPstlAdr() == null) {
            return null;
        }
        CamtPostalAddress adr = party.getPstlAdr();
        String street = Stream.of(adr.getStrtNm(), adr.getBldgNb())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
        String city = Stream.of(adr.getPstCd(), adr.getTwnNm())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
        String joined = Stream.of(street, city, adr.getCtry())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        return joined.isBlank() ? null : joined;
    }

    default BankTransactionType transactionType(CamtEntry entry) {
        CamtBankTransactionCodeFamily fmly = entry.getBkTxCd() != null && entry.getBkTxCd().getDomn() != null
                ? entry.getBkTxCd().getDomn().getFmly() : null;
        if (fmly == null) {
            return BankTransactionType.TRANSACTION;
        }
        if ("CCRD".equals(fmly.getCd()) && "POSC".equals(fmly.getSubFmlyCd())) {
            return BankTransactionType.POS_PAY;
        }
        if ("CNTR".equals(fmly.getCd()) && "CWDL".equals(fmly.getSubFmlyCd())) {
            return BankTransactionType.BANK_WITHDRAWAL;
        }
        return BankTransactionType.TRANSACTION;
    }

    default Integer parseInteger(String value) {
        return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
    }

    default String ownerAddress(CamtStatement stmt) {
        if (stmt.getAcct() == null || stmt.getAcct().getOwnr() == null
                || stmt.getAcct().getOwnr().getPstlAdr() == null
                || stmt.getAcct().getOwnr().getPstlAdr().getAdrLine() == null) {
            return null;
        }
        String joined = stmt.getAcct().getOwnr().getPstlAdr().getAdrLine().stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        return joined.isBlank() ? null : joined;
    }

    default String ownerOib(CamtStatement stmt) {
        if (stmt.getAcct() == null || stmt.getAcct().getOwnr() == null
                || stmt.getAcct().getOwnr().getId() == null
                || stmt.getAcct().getOwnr().getId().getOrgId() == null
                || stmt.getAcct().getOwnr().getId().getOrgId().getOthr() == null) {
            return null;
        }
        return stmt.getAcct().getOwnr().getId().getOrgId().getOthr().getId();
    }

    /** RptgSrc/Prtry is the reporting BIC directly followed by the bank's 11-digit OIB. */
    default String reportingPrtry(CamtStatement stmt) {
        return stmt.getRptgSrc() != null ? stmt.getRptgSrc().getPrtry() : null;
    }

    default String reportingBic(CamtStatement stmt) {
        String prtry = reportingPrtry(stmt);
        if (prtry == null || prtry.length() <= 11) {
            return prtry;
        }
        String tail = prtry.substring(prtry.length() - 11);
        return tail.chars().allMatch(Character::isDigit) ? prtry.substring(0, prtry.length() - 11) : prtry;
    }

    default String reportingOib(CamtStatement stmt) {
        String prtry = reportingPrtry(stmt);
        if (prtry == null || prtry.length() <= 11) {
            return null;
        }
        String tail = prtry.substring(prtry.length() - 11);
        return tail.chars().allMatch(Character::isDigit) ? tail : null;
    }

    default BigDecimal balance(CamtStatement stmt, String code) {
        if (stmt.getBal() == null) {
            return null;
        }
        return stmt.getBal().stream()
                .filter(b -> b.getTp() != null && b.getTp().getCdOrPrtry() != null
                        && code.equals(b.getTp().getCdOrPrtry().getCd()))
                .findFirst()
                .map(CamtStatementMapper::signedBalance)
                .orElse(null);
    }

    private static BigDecimal signedBalance(CamtBalance b) {
        if (b.getAmt() == null || b.getAmt().getValue() == null) {
            return null;
        }
        BigDecimal value = new BigDecimal(b.getAmt().getValue().trim());
        return CreditDebitIndicator.DBIT.name().equals(b.getCdtDbtInd()) ? value.negate() : value;
    }

    default CamtNumberAndSumOfTransactions summary(CamtStatement stmt, boolean credit) {
        if (stmt.getTxsSummry() == null) {
            return null;
        }
        return credit ? stmt.getTxsSummry().getTtlCdtNtries() : stmt.getTxsSummry().getTtlDbtNtries();
    }

    default Integer summaryCount(CamtStatement stmt, boolean credit) {
        CamtNumberAndSumOfTransactions s = summary(stmt, credit);
        return s != null ? parseInteger(s.getNbOfNtries()) : null;
    }

    default BigDecimal summarySum(CamtStatement stmt, boolean credit) {
        CamtNumberAndSumOfTransactions s = summary(stmt, credit);
        return s != null && s.getSum() != null && !s.getSum().isBlank() ? new BigDecimal(s.getSum().trim()) : null;
    }
}
