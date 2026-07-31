package hr.bill.spring_bill.dto.web.bill.b2c;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.TaxRate;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Schema(description = "Request for creation of F1 bill")
public class F1BillRequest extends BaseBillRequest {

    @Schema(description = "Buyers name", example = "John Doe")
    @NotNull(message = "Buyer name can't be null")
    private final String buyerName;

    @Schema(description = "Buyers residental address", example = "Ilica 1")
    @NotNull(message = "Buyer street can't be null")
    private final String buyerAddress;

    @Schema(description = "Buyers residental city", example = "Zagreb")
    @NotNull(message = "Buyer city can't be null")
    private final String buyerCity;

    @Schema(description = "Buyers residental city postal code", example = "10000")
    @NotNull(message = "Buyer postal zone can't be null")
    private final String buyerPostalCode;

    @Schema(description = "Tax rate applied to the bill", enumAsRef = true, example = "Pdv25")
    @NotNull(message = "Tax rate can't be null")
    private final TaxRate taxRate;
}