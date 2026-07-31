package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

public record DocumentStatusHistory(
        String createdOn,
        DocumentStatus status,
        String statusText,
        Double partialPaymentAmount,
        String note
) {}
