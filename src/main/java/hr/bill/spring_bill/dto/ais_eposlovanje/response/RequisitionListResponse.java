package hr.bill.spring_bill.dto.ais_eposlovanje.response;

import java.util.List;

/**
 * Returned by {@code GET /api/v2/requisitions}. Read from eposlovanje's own
 * database — does not go to the bank and does not use quota.
 */
public record RequisitionListResponse(
        Integer count,
        String next,
        String previous,
        List<RequisitionResponse> results
) {}
