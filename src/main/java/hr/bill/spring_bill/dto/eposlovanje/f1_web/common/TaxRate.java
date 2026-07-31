package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TaxRate {
    Pdv25(25.0, "PDV 25%"),
    Pdv0(0.0, "PDV 0%");

    private final Double value;
    private final String displayName;

    @JsonValue
    public Double getValue() {
        return value;
    }

    @JsonCreator
    public static TaxRate fromValue(Double value) {
        for (TaxRate t : values()) {
            if (t.value.equals(value)) return t;
        }
        throw new IllegalArgumentException("Invalid TaxRate value: " + value);
    }
}