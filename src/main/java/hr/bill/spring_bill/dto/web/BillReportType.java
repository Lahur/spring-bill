package hr.bill.spring_bill.dto.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BillReportType {
    F1_OUTGOING(2),
    F2_OUTGOING(1),
    INGOING(3),
    F2_REPORT(4);

    private final Integer order;
}
