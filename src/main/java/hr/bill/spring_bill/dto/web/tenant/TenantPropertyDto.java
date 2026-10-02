package hr.bill.spring_bill.dto.web.tenant;

import hr.bill.spring_bill.model.enums.TenantPropety;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "A tenant setting")
public record TenantPropertyDto(

        @Schema(description = "Property name", example = "STATEMENT_MAIL_TO")
        TenantPropety property,

        @Schema(description = "Property value", example = "racunovodstvo@firma.hr")
        String value
) {
}
