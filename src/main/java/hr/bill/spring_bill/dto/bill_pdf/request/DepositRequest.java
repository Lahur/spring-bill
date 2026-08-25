package hr.bill.spring_bill.dto.bill_pdf.request;

import lombok.Builder;

@Builder
public record DepositRequest(
        String creditAccount,
        String depositNumber,
        String amount,
        String amountInWords,
        String amountReceived,
        String purpose,
        String place,
        String day,
        String year,
        String liquidator,
        String cashier,
        String payerSignature
) {}
