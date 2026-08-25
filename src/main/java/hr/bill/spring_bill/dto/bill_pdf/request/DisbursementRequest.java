package hr.bill.spring_bill.dto.bill_pdf.request;

import lombok.Builder;

@Builder
public record DisbursementRequest(
        String chargedAccount,
        String disbursementNumber,
        String amount,
        String amountInWords,
        String recipientName,
        String purpose,
        String place,
        String day,
        String year,
        String liquidator,
        String cashier,
        String recipientSignature
) {}
