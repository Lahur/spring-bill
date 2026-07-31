package hr.bill.spring_bill.dto.web.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Schema(description = "Summary totals for the current month, used by the dashboard metric cards")
@Builder
public record DashboardSummaryResponse(

        @Schema(description = "Paid amount of all outgoing bill types (everything except INGOING_BILL) for the current month", example = "9500.75")
        BigDecimal salesPaidTotal,

        @Schema(description = "Unpaid amount of all outgoing bill types (everything except INGOING_BILL) for the current month", example = "3000.00")
        BigDecimal salesUnpaidTotal,

        @Schema(description = "Paid amount of INGOING_BILL bills for the current month", example = "2400.00")
        BigDecimal purchasesPaidTotal,

        @Schema(description = "Unpaid amount of INGOING_BILL bills for the current month", example = "1000.00")
        BigDecimal purchasesUnpaidTotal
) {
}