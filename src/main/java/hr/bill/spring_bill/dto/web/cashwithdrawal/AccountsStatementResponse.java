package hr.bill.spring_bill.dto.web.cashwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "An accounts statement together with its linked cash withdrawal balances")
public record AccountsStatementResponse(

        @Schema(description = "Internal ID of the accounts statement")
        UUID id,

        @Schema(description = "Statement date")
        LocalDate date,

        @Schema(description = "Statement amount")
        BigDecimal amount,

        @Schema(description = "Statement description")
        String description,

        @Schema(description = "Whether a bill PDF has been uploaded for this accounts statement")
        boolean hasBill,

        @Schema(description = "Number of times the report was sent", example = "2")
        int sentCount,

        @Schema(description = "Internal IDs of the linked cash withdrawal balances")
        Set<UUID> cashWithdrawalBalanceIds
) {
}