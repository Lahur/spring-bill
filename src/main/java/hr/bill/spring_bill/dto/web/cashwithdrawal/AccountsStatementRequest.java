package hr.bill.spring_bill.dto.web.cashwithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Builder
@Schema(description = "Request to create an accounts statement linked to cash withdrawal balances")
public record AccountsStatementRequest(

        @Schema(description = "Cash withdrawal balance IDs keyed by their order in the statement")
        @NotEmpty(message = "Cash withdrawal IDs can't be empty")
        Map<Integer, UUID> cashWithdrawalIds,

        @Schema(description = "Statement amount", example = "1000.44")
        @NotNull(message = "Amount can't be null")
        BigDecimal amount,

        @Schema(description = "Statement description", example = "Cash withdrawal at ATM")
        @NotNull(message = "Description can't be null")
        String description,

        @Schema(description = "Statement date", example = "2026-06-26")
        @NotNull(message = "Date can't be null")
        LocalDate date
) {
}