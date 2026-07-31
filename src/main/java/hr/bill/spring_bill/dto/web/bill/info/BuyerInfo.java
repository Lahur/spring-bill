package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Buyer info")
public record BuyerInfo(

        @Schema(description = "Buyers name", example = "PEVEX d.d.")
        String name,

        @Schema(description = "Buyers oib", example = "73660371074")
        String oib,

        @Schema(description = "Buyers residental address", example = "Savska cesta 84")
        String address,

        @Schema(description = "Buyers residental city", example = "Sesvete")
        String city,

        @Schema(description = "Buyers residental city postal code", example = "10360")
        String postalCode

) {
}