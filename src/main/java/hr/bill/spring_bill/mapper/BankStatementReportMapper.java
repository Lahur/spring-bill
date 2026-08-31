package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.bill_pdf.common.BankStatementLineDto;
import hr.bill.spring_bill.dto.bill_pdf.request.BankStatementRequest;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Mapper(componentModel = "spring")
public interface BankStatementReportMapper {

    DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    default BankStatementRequest toBankStatementRequest(BankStatementEntity statement, List<BankTransactionEntity> transactions) {
        AtomicInteger row = new AtomicInteger(1);
        return BankStatementRequest.builder()
                .bankName(bankName(statement.getBankBic()))
                .bankAddress(bankAddress(statement.getBankBic()))
                .bankOib(statement.getBankOib())
                .bankBic(shortBic(statement.getBankBic()))
                .statementNumber(statement.getSequenceNumber() != null
                        ? String.valueOf(statement.getSequenceNumber())
                        : statement.getStatementId())
                .statementDate(formatDate(statement.getCreatedAt()))
                .accountIban(statement.getIban())
                .accountName(statement.getAccountName())
                .currency(statement.getCurrency())
                .ownerName(statement.getOwnerName())
                .ownerAddress(statement.getOwnerAddress())
                .ownerOib(statement.getOwnerOib())
                .periodFrom(formatDate(statement.getPeriodFrom()))
                .periodTo(formatDate(statement.getPeriodTo()))
                .openingBalance(formatAmount(statement.getOpeningBalance()))
                .closingBalance(formatAmount(statement.getClosingBalance()))
                .creditCount(statement.getCreditCount() != null ? String.valueOf(statement.getCreditCount()) : null)
                .creditSum(formatAmount(statement.getCreditSum()))
                .debitCount(statement.getDebitCount() != null ? String.valueOf(statement.getDebitCount()) : null)
                .debitSum(formatAmount(statement.getDebitSum()))
                .transactions(transactions.stream()
                        .map(tx -> toLine(tx, row.getAndIncrement()))
                        .toList())
                .build();
    }

    default BankStatementLineDto toLine(BankTransactionEntity tx, int rowNumber) {
        boolean debit = tx.getCreditDebitIndicator() == CreditDebitIndicator.DBIT;
        return BankStatementLineDto.builder()
                .rowNumber(rowNumber)
                .bookingDate(formatDate(tx.getTransactionDate()))
                .valueDate(formatDate(tx.getValueDate()))
                .entryReference(tx.getEntryReference())
                .transactionReference(tx.getTransactionReference())
                .counterpartyIban(debit ? tx.getReceiverIban() : tx.getSenderIban())
                .counterpartyName(tx.getCounterpartyName())
                .counterpartyAddress(tx.getCounterpartyAddress())
                .payerReference(tx.getPayerReference())
                .payeeReference(tx.getReference())
                .description(tx.getAdditionalRemittanceInfo())
                .debitAmount(debit ? formatAmount(tx.getAmount()) : null)
                .creditAmount(debit ? null : formatAmount(tx.getAmount()))
                .build();
    }

    default String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("hr"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", symbols).format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    default String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE);
    }

    default String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE);
    }

    /** BIC as printed on the statement head - the 8-char institution code. */
    default String shortBic(String bic) {
        if (bic == null) {
            return null;
        }
        return bic.length() >= 8 ? bic.substring(0, 8) : bic;
    }

    default String bankName(String bic) {
        if (bic == null) {
            return null;
        }
        return bic.startsWith("ZABAHR2X") ? "Zagrebačka banka d.d." : null;
    }

    default String bankAddress(String bic) {
        if (bic == null) {
            return null;
        }
        return bic.startsWith("ZABAHR2X") ? "Trg bana Josipa Jelačića 10, 10000 Zagreb" : null;
    }
}
