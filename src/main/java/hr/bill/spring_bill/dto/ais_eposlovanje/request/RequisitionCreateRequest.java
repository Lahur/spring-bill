package hr.bill.spring_bill.dto.ais_eposlovanje.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/** Body of {@code POST /api/v2/requisitions}. */
@Builder
public record RequisitionCreateRequest(
        @JsonProperty("institution_id") String institutionId,
        /** Days of transaction history to request. Optional; server default applies when omitted. */
        @JsonProperty("max_historical_days") Integer maxHistoricalDays
) {}
