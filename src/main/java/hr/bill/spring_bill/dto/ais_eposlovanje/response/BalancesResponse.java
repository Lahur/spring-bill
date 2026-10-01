package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import hr.bill.spring_bill.dto.ais_eposlovanje.common.Money;

import java.util.List;

/**
 * Returned by {@code GET /api/v2/accounts/{id}/balances}. Goes to the bank and
 * uses the account's daily quota.
 */
public record BalancesResponse(List<Balance> balances) {

    public record Balance(Money balanceAmount, String balanceType, String referenceDate) {}
}
