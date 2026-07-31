package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Bill item info, merged across B2B and B2C bills")
public record BillItemInfo(

        @Schema(description = "Item name", example = "Parking lot")
        String name,

        @Schema(description = "Item description", example = "Parking lot for Pevex")
        String description,

        @Schema(description = "Item quantity", example = "1")
        BigDecimal quantity,

        @Schema(description = "Quantity unit of measure, merged across B2B and B2C bills", enumAsRef = true, example = "H87")
        ItemUnitOfMeasure unitOfMeasure,

        @Schema(description = "Item unit price before tax (B2C only)", example = "1000.44")
        BigDecimal unitPrice,

        @Schema(description = "Item amount before tax", example = "1000.44")
        BigDecimal baseAmount,

        @Schema(description = "Item amount including tax", example = "1250.55")
        BigDecimal totalAmount,

        @Schema(description = "Tax amount for item (B2C only)", example = "250.11")
        BigDecimal taxAmount,

        @Schema(description = "VAT rate/category applied to item, merged across B2B and B2C bills", enumAsRef = true, example = "Pdv25")
        BillVatRate vatRate,

        @Schema(description = "Currency of the item (B2C only)", example = "EUR")
        String currency
) {
}