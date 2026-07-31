package hr.bill.spring_bill.dto.bill_pdf.common;

public record IncomingPaymentMeansDto(
        String iban,
        String paymentId,
        String instructionId,
        String instructionNote,
        String paymentDueDate,
        String accountCurrencyCode,
        String paymentChannelCode
) {}
