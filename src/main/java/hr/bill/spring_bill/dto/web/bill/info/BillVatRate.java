package hr.bill.spring_bill.dto.web.bill.info;

import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.TaxRate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "VAT rate/category applied to a bill or bill item, merged across B2B and B2C bills")
@RequiredArgsConstructor
@Getter
public enum BillVatRate {
    Pdv25(BigDecimal.valueOf(25), "PDV 25%"),
    Pdv0(BigDecimal.ZERO, "PDV 0%"),
    ReverseCharge(BigDecimal.ZERO, "Prijenos porezne obveze");

    private final BigDecimal rate;
    private final String displayName;

    public static BillVatRate fromVatCategory(VatCategory vatCategory) {
        if (vatCategory == null) {
            return null;
        }
        return switch (vatCategory) {
            case Pdv25 -> Pdv25;
            case ReverseCharge -> ReverseCharge;
        };
    }

    public static BillVatRate fromTaxRate(TaxRate taxRate) {
        if (taxRate == null) {
            return null;
        }
        return switch (taxRate) {
            case Pdv25 -> Pdv25;
            case Pdv0 -> Pdv0;
        };
    }
}