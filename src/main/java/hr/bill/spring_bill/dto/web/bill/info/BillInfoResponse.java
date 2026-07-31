package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Bill info response, merged across B2B and B2C bills")
public record BillInfoResponse(

        MainDataInfo mainDataInfo,

        BuyerInfo buyerInfo,

        SupplierInfo supplierInfo,

        List<BillItemInfo> itemInfos,

        @Schema(description = "Payment info (present for B2B bills only)")
        PaymentInfo paymentInfo,

        @Schema(description = "Fiscalization info (present for B2C bills only)")
        FiscalInfo fiscalInfo,

        PriceInfo priceInfo

) {
}