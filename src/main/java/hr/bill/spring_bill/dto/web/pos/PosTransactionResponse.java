package hr.bill.spring_bill.dto.web.pos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Schema(description = "A POS transaction together with its linked bank transaction data")
public record PosTransactionResponse(

        @Schema(description = "Internal ID of the POS transaction")
        UUID id,

        @Schema(description = "Internal ID of the linked bank transaction")
        UUID bankTransactionId,

        @Schema(description = "Transaction amount")
        BigDecimal amount,

        @Schema(description = "IBAN of the sender")
        String senderIban,

        @Schema(description = "IBAN of the receiver")
        String receiverIban,

        @Schema(description = "Payment reference")
        String reference,

        @Schema(description = "Additional remittance information")
        String additionalRemittanceInfo,

        @Schema(description = "Date and time of the transaction")
        LocalDateTime transactionDate,

        @Schema(description = "Whether a bill PDF has been uploaded for this POS transaction")
        boolean hasBill,

        @Schema(description = "Number of times the report was sent", example = "2")
        int sentCount
) {
}