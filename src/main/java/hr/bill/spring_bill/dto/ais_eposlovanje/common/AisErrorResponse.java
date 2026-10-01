package hr.bill.spring_bill.dto.ais_eposlovanje.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error body returned by the AIS API on non-2xx responses, e.g.
 * {@code {"summary": "Rate limit exceeded", "detail": "...", "status_code": 429}}.
 * Feign throws {@code FeignException} for these; callers who need the message
 * decode {@code FeignException#contentUTF8()} into this shape themselves.
 */
public record AisErrorResponse(
        String summary,
        String detail,
        @JsonProperty("status_code") Integer statusCode
) {}
