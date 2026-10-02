package hr.bill.spring_bill.dto.web.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Tenant (bill issuer) data used on outgoing bills and bank statements")
public record TenantDto(

        @Schema(description = "Company OIB", example = "12345678901")
        @NotBlank(message = "OIB can't be blank")
        String oib,

        @Schema(description = "Company name", example = "Firma d.o.o.")
        @NotBlank(message = "Name can't be blank")
        String name,

        @Schema(description = "Street and house number", example = "Ilica 1")
        @NotBlank(message = "Street can't be blank")
        String street,

        @Schema(description = "City", example = "Zagreb")
        @NotBlank(message = "City can't be blank")
        String city,

        @Schema(description = "Postal code", example = "10000")
        @NotBlank(message = "Postal zone can't be blank")
        String postalZone,

        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "HR")
        @NotBlank(message = "Country code can't be blank")
        String countryCode,

        @Schema(description = "Contact person / operator OIB", example = "12345678901")
        @NotBlank(message = "Contact OIB can't be blank")
        String contactOib,

        @Schema(description = "Contact person name", example = "Ivan Horvat")
        @NotBlank(message = "Contact name can't be blank")
        String contactName,

        @Schema(description = "Contact phone", example = "+385911234567")
        String phone,

        @Schema(description = "Contact email", example = "info@firma.hr")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Company IBAN", example = "HR1210010051863000160")
        @NotBlank(message = "IBAN can't be blank")
        String iban
) {
}
