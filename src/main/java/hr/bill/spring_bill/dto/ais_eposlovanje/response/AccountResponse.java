package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Returned by {@code GET /api/v2/accounts/{id}}. Local data only — does not go
 * to the bank and does not use the account's daily quota.
 */
public record AccountResponse(
        String id,
        String created,
        @JsonProperty("last_accessed") String lastAccessed,
        String iban,
        @JsonProperty("institution_id") String institutionId,
        String status,
        @JsonProperty("owner_name") String ownerName
) {}
