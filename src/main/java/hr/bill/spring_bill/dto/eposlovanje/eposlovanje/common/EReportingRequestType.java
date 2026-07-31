package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EReportingRequestType {
    EvidencijaIzlaznogRacuna(1),
    EvidencijaUlaznogRacuna(2),
    EvidencijaIsporuke(3),
    EvidencijaNavlate(4),
    EvidencijaOdbijanja(5);

    private final int value;

    public static EReportingRequestType fromValue(int value) {
        for (EReportingRequestType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid EReportingRequestType value: " + value);
    }
}