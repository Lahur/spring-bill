package hr.bill.spring_bill.dto.web.bill.b2b;

import hr.bill.spring_bill.dto.eposlovanje.enums.BillProfile;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request for creation of bill report")
public class ReportBillRequest extends BaseBillRequest {

    @Schema(description = "Bill id", example = "1")
    @NotNull(message = "Bill id can't be null")
    private Integer billId;

    @Schema(description = "Bill profile", enumAsRef = true, example = "P1")
    @NotNull(message = "Bill profile can't be null")
    private BillProfile profile;

    @Schema(description = "Type of VAT used on bill", enumAsRef = true, example = "Pdv25")
    @NotNull(message = "Vat category can't be null")
    private VatCategory vatCategory;

    @Schema(description = "Order number this bill refers to", example = "indent P-1-100")
    private String orderNumber;

    @Schema(description = "Buyers name", example = "PEVEX d.d.")
    @NotNull(message = "Buyer name can't be null")
    private String buyerName;

    @Schema(description = "Buyers residental address", example = "Savska cesta 84")
    @NotNull(message = "Buyer street can't be null")
    private String buyerStreet;

    @Schema(description = "Buyers residental city", example = "Sesvete")
    @NotNull(message = "Buyer city can't be null")
    private String buyerCity;

    @Schema(description = "Buyers residental city postal code", example = "10360")
    @NotNull(message = "Buyer postal zone can't be null")
    private String buyerPostalZone;

    @Schema(hidden = true)
    private byte[] orderDocumentBytes;

    @Schema(hidden = true)
    private String orderDocumentFilename;
}
