package hr.bill.spring_bill.dto.web.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Persisted summary totals for a single completed month")
@Builder
public record MonthlySummaryResponse(

        @Schema(description = "First day of the month", example = "2026-06-01")
        LocalDate month,

        @Schema(description = "Paid amount of all outgoing bill types (everything except INGOING_BILL) for the month", example = "9500.75")
        BigDecimal salesPaidTotal,

        @Schema(description = "Unpaid amount of all outgoing bill types (everything except INGOING_BILL) for the month", example = "3000.00")
        BigDecimal salesUnpaidTotal,

        @Schema(description = "Paid amount of INGOING_BILL bills for the month", example = "2400.00")
        BigDecimal purchasesPaidTotal,

        @Schema(description = "Unpaid amount of INGOING_BILL bills for the month", example = "1000.00")
        BigDecimal purchasesUnpaidTotal
) {
}