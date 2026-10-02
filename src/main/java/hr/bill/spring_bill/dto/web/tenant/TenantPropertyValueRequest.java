package hr.bill.spring_bill.dto.web.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "New value for a tenant setting. STATEMENT_MAIL_TO must be an email; "
        + "DISBURSEMENT_COUNT and DEPOSIT_COUNT a non-negative whole number (the last number issued)")
public record TenantPropertyValueRequest(

        @Schema(description = "Property value", example = "racunovodstvo@firma.hr")
        @NotBlank(message = "Value can't be blank; use DELETE to remove a property")
        String value
) {
}
