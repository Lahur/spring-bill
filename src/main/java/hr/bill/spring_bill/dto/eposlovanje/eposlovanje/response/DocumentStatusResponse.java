package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatus;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatusHistory;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.EReportingRequestItem;

import java.util.List;

public record DocumentStatusResponse(
        Long id,
        String insertedOn,
        String modifiedOn,
        String documentId,
        String documentType,
        String issuedOn,
        Double amount,
        String currency,
        String supplierPartyName,
        String supplierPartyVATId,
        String supplierPartyBusinessUnit,
        String supplierPartyGLN,
        String customerPartyName,
        String customerPartyVATId,
        String customerPartyBusinessUnit,
        String customerPartyGLN,
        DocumentStatus status,
        List<DocumentStatusHistory> statusHistory,
        List<EReportingRequestItem> eReportingRequests
) {}
