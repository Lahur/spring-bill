package hr.bill.spring_bill.dto.bill_pdf.request;

import lombok.Builder;

@Builder
public record PosTransactionRequest(
        String transactionId,
        String bankTransactionId,
        String amount,
        String currencyCode,
        String senderIban,
        String receiverIban,
        String reference,
        String additionalRemittanceInfo,
        String transactionDate,
        String transactionTime,
        boolean hasBill
) {}
