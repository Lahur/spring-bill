package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UnitOfMeasure {
    Kom("kom", "Komad"),
    Kg("kg", "Kilogram"),
    L("l", "Litra"),
    M("m", "Metar"),
    H("h", "Sat");

    private final String code;
    private final String displayName;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static UnitOfMeasure fromCode(String code) {
        for (UnitOfMeasure u : values()) {
            if (u.code.equalsIgnoreCase(code)) return u;
        }
        throw new IllegalArgumentException("Invalid UnitOfMeasure code: " + code);
    }
}