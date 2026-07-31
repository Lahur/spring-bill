package hr.bill.spring_bill.dto.bill_pdf.common;

public record OutgoingPaymentMeansDto(
        String code,
        String dueDate,
        String channelCode,
        String instructionNote,
        String paymentId,
        String iban,
        String accountCurrencyCode
) {}
