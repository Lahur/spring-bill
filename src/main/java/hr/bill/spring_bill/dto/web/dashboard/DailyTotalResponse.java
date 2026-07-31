package hr.bill.spring_bill.dto.web.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Total sales amount for a single day")
@Builder
public record DailyTotalResponse(

        @Schema(description = "Date", example = "2026-07-01")
        LocalDate date,

        @Schema(description = "Total amount for that day", example = "540.00")
        BigDecimal total
) {
}