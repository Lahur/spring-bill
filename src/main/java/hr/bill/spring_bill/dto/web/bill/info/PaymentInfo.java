package hr.bill.spring_bill.dto.web.bill.info;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Bill payment info (B2B only)")
public record PaymentInfo(

        @Schema(description = "Method of paying the bill, merged across B2B and B2C bills", enumAsRef = true, example = "CreditTransfer")
        BillPaymentMethod paymentMethod,

        @Schema(description = "Date until bill should be paid", example = "2026-07-15")
        LocalDate dueDate,

        @Schema(description = "Bank account number of the payee", example = "HR1210010051863000160")
        String iban,

        @Schema(description = "Model of payment", example = "HR00")
        String model,

        @Schema(description = "Payment reference number used to identify the payer and purpose of the payment", example = "12-3-4")
        String reference,

        @Schema(description = "Payment description", example = "payment for bill 12-3-4")
        String note
) {
}