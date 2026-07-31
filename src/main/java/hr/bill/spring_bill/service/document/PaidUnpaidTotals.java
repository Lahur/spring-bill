package hr.bill.spring_bill.service.document;

import java.math.BigDecimal;

public record PaidUnpaidTotals(BigDecimal paid, BigDecimal unpaid) {

    public static PaidUnpaidTotals zero() {
        return new PaidUnpaidTotals(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public PaidUnpaidTotals add(PaidUnpaidTotals other) {
        return new PaidUnpaidTotals(paid.add(other.paid), unpaid.add(other.unpaid));
    }
}