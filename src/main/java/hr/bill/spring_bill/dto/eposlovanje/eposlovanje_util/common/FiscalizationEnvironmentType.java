package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FiscalizationEnvironmentType {
    Test(0),
    Production(1);

    private final int value;

    public static FiscalizationEnvironmentType fromValue(int value) {
        for (FiscalizationEnvironmentType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid FiscalizationEnvironmentType: " + value);
    }
}