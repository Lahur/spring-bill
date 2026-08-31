package hr.bill.spring_bill.dto.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Schema(description = "A bank statement previously imported into the system")
public record BankStatementResponse(

        @Schema(description = "Internal ID")
        UUID id,

        @Schema(description = "Statement ID as assigned by the bank")
        String statementId,

        @Schema(description = "IBAN of the account the statement is for")
        String iban,

        @Schema(description = "Currency of the account")
        String currency,

        @Schema(description = "Start date of the statement period")
        LocalDate periodFrom,

        @Schema(description = "End date of the statement period")
        LocalDate periodTo,

        @Schema(description = "When the statement was created by the bank")
        LocalDateTime createdAt,

        @Schema(description = "Number of times the statement PDF has been sent", example = "2")
        int sentCount
) {
}
