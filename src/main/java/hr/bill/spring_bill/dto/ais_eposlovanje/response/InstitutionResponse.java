package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** A bank as returned by {@code GET /api/v2/institutions}. */
public record InstitutionResponse(
        String id,
        String name,
        String bic,
        @JsonProperty("transaction_total_days") String transactionTotalDays,
        List<String> countries,
        String logo
) {}
