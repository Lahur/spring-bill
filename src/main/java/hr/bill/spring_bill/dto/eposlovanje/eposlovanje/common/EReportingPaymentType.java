package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EReportingPaymentType {
    TransakcijskiRacun(1),
    ObracunskoPlaćanje(2),
    Ostalo(3);

    private final int value;

    public static EReportingPaymentType fromValue(int value) {
        for (EReportingPaymentType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid EReportingPaymentType value: " + value);
    }
}