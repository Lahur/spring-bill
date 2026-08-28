package hr.bill.spring_bill.dto.web.cashwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "A cash withdrawal balance together with its linked bank transaction and accounts statements")
public record CashWithdrawalBalanceResponse(

        @Schema(description = "Internal ID of the cash withdrawal balance")
        UUID id,

        @Schema(description = "Total withdrawn amount")
        BigDecimal total,

        @Schema(description = "Remaining balance")
        BigDecimal balance,

        @Schema(description = "Internal ID of the linked bank transaction")
        UUID bankTransactionId,

        @Schema(description = "Bank transaction amount")
        BigDecimal amount,

        @Schema(description = "Additional remittance information")
        String additionalRemittanceInfo,

        @Schema(description = "Date and time of the transaction")
        LocalDateTime transactionDate,

        @Schema(description = "Number of times the report was sent", example = "2")
        int sentCount,

        @Schema(description = "Internal IDs of the linked accounts statements")
        Set<UUID> accountsStatementIds
) {
}