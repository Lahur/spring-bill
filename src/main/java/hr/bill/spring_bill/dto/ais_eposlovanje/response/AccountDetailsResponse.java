package hr.bill.spring_bill.dto.ais_eposlovanje.response;

/**
 * Returned by {@code GET /api/v2/accounts/{id}/details}. Goes to the bank and
 * uses the account's daily quota.
 */
public record AccountDetailsResponse(AccountDetails account) {

    /** Fields are already camelCase as the bank (Berlin Group PSD2) sends them. */
    public record AccountDetails(
            String resourceId,
            String iban,
            String currency,
            String ownerName,
            String product,
            String cashAccountType,
            String status
    ) {}
}
