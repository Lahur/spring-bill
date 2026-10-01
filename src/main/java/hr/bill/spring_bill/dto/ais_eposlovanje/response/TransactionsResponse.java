package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import hr.bill.spring_bill.dto.ais_eposlovanje.common.IbanAccount;
import hr.bill.spring_bill.dto.ais_eposlovanje.common.Money;

import java.util.List;

/**
 * Returned by {@code GET /api/v2/accounts/{id}/transactions}. Goes to the bank
 * and uses the account's daily quota (typically 4 calls/day) — retrieve once a
 * day and store the result rather than polling.
 */
public record TransactionsResponse(Transactions transactions) {

    public record Transactions(List<Transaction> booked, List<Transaction> pending) {}

    public record Transaction(
            String transactionId,
            String endToEndId,
            String bookingDate,
            String valueDate,
            Money transactionAmount,
            String creditorName,
            IbanAccount creditorAccount,
            String debtorName,
            IbanAccount debtorAccount,
            String remittanceInformationUnstructured,
            String remittanceInformationStructured
    ) {}
}
