package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.BillReportType;

public interface DocumentStrategy {

    BillReportType getType();

    BillDocument createDocument(String id);

    void incrementSentCount(String id);
}
