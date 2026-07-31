package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EReportingRejectionType {
    NeutjeceNaPorez(1),
    UtjeceNaPorez(2),
    Ostalo(3);

    private final int value;

    public static EReportingRejectionType fromValue(int value) {
        for (EReportingRejectionType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid EReportingRejectionType value: " + value);
    }
}