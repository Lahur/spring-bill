package hr.bill.spring_bill.dto.web.bill;

import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response containing bill details")
@Builder(toBuilder = true)
public record BillResponse(

        @Schema(description = "Bill id")
        UUID id,

        @Schema(description = "System bill id", example = "1")
        Long systemId,

        @Schema(description = "Presentable bill id", example = "219483")
        String fullBillId,

        @Schema(description = "Client name", example = "PEVEX d.d.")
        String clientName,

        @Schema(description = "Client oib", example = "73660371074")
        String clientOib,

        @Schema(description = "Date and time when bill was created", example = "2026-06-26T10:15:30")
        LocalDateTime billDate,

        @Schema(description = "Total amount for bill including tax", example = "1250.55")
        BigDecimal totalAmount,

        @Schema(description = "Status of the bill", example = "Isporucen")
        BillDocumentStatus documentStatus,

        @Schema(description = "Type of the bill", example = "F2_BILL")
        BillType billType,

        @Schema(description = "Number of times the bill was sent", example = "2")
        int sentCount
) {
}