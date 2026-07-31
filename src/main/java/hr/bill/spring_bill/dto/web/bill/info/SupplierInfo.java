package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supplier info, merged across B2B and B2C bills")
public record SupplierInfo(

        @Schema(description = "Suppliers name", example = "TEHNOMODUS d.o.o. za projektiranje i usluge")
        String name,

        @Schema(description = "Suppliers oib", example = "31728187872")
        String oib,

        @Schema(description = "Suppliers residental address (B2B only)", example = "Ulica Ljudevita Posavskog 34/A")
        String address,

        @Schema(description = "Suppliers residental city (B2B only)", example = "Zagreb")
        String city,

        @Schema(description = "Suppliers residental city postal code (B2B only)", example = "10000")
        String postalCode,

        @Schema(description = "Contact name (B2B only)", example = "Karlo Belavić")
        String contactName,

        @Schema(description = "Contact oib (B2B only)", example = "78839786001")
        String contactOib,

        @Schema(description = "Contact email (B2B only)", example = "lahur.123@gmail.com")
        String contactEmail,

        @Schema(description = "Contact phone (B2B only)", example = "+385916067001")
        String contactPhone
) {
}