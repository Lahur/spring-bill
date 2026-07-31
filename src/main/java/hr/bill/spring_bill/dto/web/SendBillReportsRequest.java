package hr.bill.spring_bill.dto.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Request to send bill reports by email")
public record SendBillReportsRequest(

        @Schema(description = "Bill reports to send")
        @NotEmpty(message = "Reports can't be empty")
        @Valid
        List<SendBillReportItem> reports,

        @Schema(description = "Recipient email address", example = "buyer@example.com")
        @NotNull(message = "Email can't be null")
        String email
) {}