package hr.bill.spring_bill.dto.web.bill.info;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Main bill info, merged across B2B and B2C bills")
public record MainDataInfo(

        @Schema(description = "Date when bill was created (B2B only)", example = "2026-06-26")
        LocalDate billDate,

        @Schema(description = "Date and time when the receipt was issued (B2C only)", example = "2026-06-26T11:00:00")
        LocalDateTime issueDateTime,

        @Schema(description = "Date until bill should be paid", example = "2026-07-15")
        LocalDate dueDate,

        @Schema(description = "Kind of bill document, merged across B2B and B2C bills", enumAsRef = true, example = "CommercialInvoice")
        BillDocumentKind documentKind,

        @Schema(description = "Payment method used for the bill (B2C only)", enumAsRef = true, example = "CreditTransfer")
        BillPaymentMethod paymentMethod,

        @Schema(description = "Status of fiscalization for the receipt (B2C only)", enumAsRef = true, example = "Success")
        FiscalStatus fiscalStatus,

        @Schema(description = "Currency of the bill (B2B only)", enumAsRef = true, example = "EUR")
        String currency,

        @Schema(description = "Period of bill scope from (B2B only)", example = "2026-06-01")
        LocalDate billPeriodFrom,

        @Schema(description = "Period of bill scope till (B2B only)", example = "2026-06-25")
        LocalDate billPeriodTill
) {
}