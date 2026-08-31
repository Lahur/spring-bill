package hr.bill.spring_bill.service.document;

import java.math.BigDecimal;

public record PaidUnpaidTotals(BigDecimal paid, BigDecimal unpaid) {

    public static PaidUnpaidTotals zero() {
        return new PaidUnpaidTotals(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public PaidUnpaidTotals add(PaidUnpaidTotals other) {
        return new PaidUnpaidTotals(paid.add(other.paid), unpaid.add(other.unpaid));
    }

    /**
     * A negative unpaid total can arise when storno / credit-note bills in the period outweigh the
     * open ones; floor it at zero before surfacing the figure to users.
     */
    public PaidUnpaidTotals withNonNegativeUnpaid() {
        return unpaid.signum() < 0 ? new PaidUnpaidTotals(paid, BigDecimal.ZERO) : this;
    }
}