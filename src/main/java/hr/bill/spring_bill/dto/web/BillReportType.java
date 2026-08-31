package hr.bill.spring_bill.dto.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BillReportType {
    F1_OUTGOING(2),
    F2_OUTGOING(1),
    INGOING(3),
    F2_REPORT(4),
    CASH_WITHDRAWAL(5),
    ACCOUNTS_STATEMENT(6),
    POS(7),
    BANK_STATEMENT(8);

    private final Integer order;
}
