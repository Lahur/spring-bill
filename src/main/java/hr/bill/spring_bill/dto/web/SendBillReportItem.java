package hr.bill.spring_bill.dto.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Single bill report to be sent")
public record SendBillReportItem(

        @Schema(description = "Internal id of the bill report", example = "1")
        @NotNull(message = "Id can't be null")
        String id,

        @Schema(description = "Bill id", example = "1/1/1")
        @NotNull(message = "Bill id can't be null")
        String billId,

        @Schema(description = "Type of the bill report", enumAsRef = true, example = "F2_OUTGOING")
        @NotNull(message = "Type can't be null")
        BillReportType type
) {}