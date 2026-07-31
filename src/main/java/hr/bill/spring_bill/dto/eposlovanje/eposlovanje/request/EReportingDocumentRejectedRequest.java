package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.EReportingRejectionType;
import lombok.Builder;

@Builder
public record EReportingDocumentRejectedRequest(
        String documentId,
        String issueDate,
        String supplierPartyId,
        String customerPartyId,
        String rejectionDate,
        EReportingRejectionType rejectionType,
        String rejectionReason
) {}
