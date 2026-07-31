package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Bill price info, merged across B2B and B2C bills")
public record PriceInfo(

        @Schema(description = "Bill amount without VAT (PDV)", example = "1000.44")
        BigDecimal vatExclusiveAmount,

        @Schema(description = "VAT (PDV) amount", example = "250.11")
        BigDecimal vatAmount,

        @Schema(description = "Bill amount with VAT (PDV)", example = "1250.55")
        BigDecimal vatInclusiveAmount,

        @Schema(description = "Amount payed in advance (B2B only)", example = "150.11")
        BigDecimal advanceAmount,

        @Schema(description = "Total amount to pay (B2B only)", example = "1100.44")
        BigDecimal totalAmount

) {
}