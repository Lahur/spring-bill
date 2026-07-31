package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

public record EReportingRequestItem(
        EReportingRequestType type,
        String description,
        String createdOn,
        Boolean success,
        String errorCode,
        String errorDescription,
        String completedOn,
        String referenceId,
        String paymentDate,
        Double paidAmount,
        EReportingPaymentType paymentType,
        String rejectionDate,
        EReportingRejectionType rejectionType,
        String rejectionReason
) {}
