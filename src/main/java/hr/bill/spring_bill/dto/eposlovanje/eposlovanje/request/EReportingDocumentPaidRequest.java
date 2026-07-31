package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.EReportingPaymentType;
import lombok.Builder;

@Builder
public record EReportingDocumentPaidRequest(
        String documentId,
        String issueDate,
        String supplierPartyId,
        String customerPartyId,
        String paymentDate,
        Double paidAmount,
        EReportingPaymentType paymentType
) {}
