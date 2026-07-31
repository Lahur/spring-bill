package hr.bill.spring_bill.dto.web.bill;

import hr.bill.spring_bill.dto.eposlovanje.enums.BillProfile;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Schema(description = "Response containing bill details for review")
public record BillReviewResponse(

        @Schema(description = "Full bills id", example = "1/1/1")
        String billNumber,

        @Schema(description = "Bills profile that determines what kind of bill it is", enumAsRef = true, example = "P1")
        BillProfile profile,

        @Schema(description = "Date when bill was created", example = "2026-06-26")
        LocalDate billDate,

        @Schema(description = "Time when bill was created", example = "11:00:00")
        LocalTime billTime,

        @Schema(description = "Date until bill should be paid", example = "2026-07-15")
        LocalDate dueDate,

        @Schema(description = "Additional note for the bill", example = "Some amount added")
        String note,

        @Schema(description = "Reference to a bill", example = "indent P-1-100")
        String reference,

        @Schema(description = "Buyers OIB", example = "73660371074")
        String buyerOib,

        @Schema(description = "Buyers name", example = "PEVEX d.d.")
        String buyerName,

        @Schema(description = "Buyers residental address", example = "Savska cesta 84")
        String buyerAddress,

        @Schema(description = "Buyers residental city", example = "Sesvete")
        String buyerCity,

        @Schema(description = "Buyers residental city postal code", example = "10360")
        String buyerPostalZone,

        @Schema(description = "Base amount for bill", example = "1000.44")
        BigDecimal baseAmount,

        @Schema(description = "Calculated tax amount for bill", example = "250.11")
        BigDecimal taxAmount,

        @Schema(description = "Total amount for bill including tax", example = "1250.55")
        BigDecimal totalAmount,

        @Schema(description = "Type of VAT used on bill", enumAsRef = true, example = "Pdv25")
        VatCategory pdvType,

        @Schema(description = "Name of the bill item", example = "Parking lot")
        String name,

        @Schema(description = "Description of bill item", example = "Parking lot for Pevex")
        String description
) {
}