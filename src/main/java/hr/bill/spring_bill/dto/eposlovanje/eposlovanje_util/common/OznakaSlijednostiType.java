package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OznakaSlijednostiType {
    PoslovniProstor(0),
    Uredjaj(1);

    private final int value;

    public static OznakaSlijednostiType fromValue(int value) {
        for (OznakaSlijednostiType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid OznakaSlijednostiType: " + value);
    }
}