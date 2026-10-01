package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Returned by {@code POST /api/v2/requisitions}, {@code GET /api/v2/requisitions/{id}}
 * and as each item of {@code GET /api/v2/requisitions}'s {@code results}. Poll the
 * single-id GET until {@code status} is {@code LN} ({@code RJ} rejected, {@code EX}
 * expired); {@code accounts} is then populated with the approved account ids.
 */
public record RequisitionResponse(
        String id,
        String created,
        String status,
        @JsonProperty("institution_id") String institutionId,
        String agreement,
        String reference,
        List<String> accounts,
        String link,
        String redirect
) {}
