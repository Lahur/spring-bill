package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record EReportingReportDocumentRequest(
        String document,
        String documentType
) {}
