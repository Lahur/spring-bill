package hr.bill.spring_bill.dto.web.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "Where imported bank statements are mailed when bill.statement.mail-enabled is on")
public record StatementMailToDto(

        @Schema(description = "Recipient email; null or blank stops mailing statements for this tenant",
                example = "racunovodstvo@firma.hr")
        @Email(message = "Mail-to must be a valid email")
        String mailTo
) {
}
