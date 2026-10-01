package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.ais_eposlovanje.common.IbanAccount;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.TransactionsResponse.Transaction;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AisTransactionMapper {

    int TEXT_MAX_LENGTH = 140;

    /**
     * The AIS API sends a signed amount: negative for money leaving the account (debtor is
     * the account owner), positive for money coming in (creditor is the account owner).
     */
    default BankTransactionEntity toBankTransactionEntity(Transaction tx, UUID bankStatementId, BankTransactionType transactionType) {
        BigDecimal amount = signedAmount(tx);
        boolean debit = amount.signum() < 0;
        return BankTransactionEntity.builder()
                .bankStatement(BankStatementEntity.builder().id(bankStatementId).build())
                .amount(amount.abs())
                .creditDebitIndicator(debit ? CreditDebitIndicator.DBIT : CreditDebitIndicator.CRDT)
                .senderIban(iban(tx.debtorAccount()))
                .receiverIban(iban(tx.creditorAccount()))
                .counterpartyName(truncate(counterpartyName(tx, debit)))
                .reference(reference(tx.remittanceInformationStructured()))
                .payerReference(tx.endToEndId())
                .transactionReference(tx.transactionId())
                .additionalRemittanceInfo(truncate(tx.remittanceInformationUnstructured()))
                .transactionDate(bookingDate(tx).atStartOfDay())
                .valueDate(tx.valueDate() != null ? LocalDate.parse(tx.valueDate()) : null)
                .transactionType(transactionType)
                .build();
    }

    /** Card transactions have no creditor name; camt imports store the account owner (debtor) for them. */
    default String counterpartyName(Transaction tx, boolean debit) {
        if (!debit) {
            return tx.debtorName();
        }
        return tx.creditorName() != null ? tx.creditorName() : tx.debtorName();
    }

    default BigDecimal signedAmount(Transaction tx) {
        return new BigDecimal(tx.transactionAmount().amount().trim());
    }

    default LocalDate bookingDate(Transaction tx) {
        return LocalDate.parse(tx.bookingDate() != null ? tx.bookingDate() : tx.valueDate());
    }

    default String iban(IbanAccount account) {
        return account != null ? account.iban() : null;
    }

    /** The API formats the model and reference as "HR01 79427-2824077264"; camt imports store it without the space. */
    default String reference(String structured) {
        if (structured == null || structured.isBlank()) {
            return null;
        }
        return structured.replace(" ", "");
    }

    default String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > TEXT_MAX_LENGTH ? value.substring(0, TEXT_MAX_LENGTH) : value;
    }
}
