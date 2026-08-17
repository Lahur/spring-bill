package hr.bill.spring_bill.dto.web.pos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Request to generate POS transaction reports and send them by email")
public record PosTransactionGenerateAndSendRequest(

        @Schema(description = "IDs of POS transactions to generate reports for")
        @NotEmpty(message = "Ids can't be empty")
        List<UUID> ids,

        @Schema(description = "Recipient email address", example = "buyer@example.com")
        @NotNull(message = "Email can't be null")
        String email
) {}
