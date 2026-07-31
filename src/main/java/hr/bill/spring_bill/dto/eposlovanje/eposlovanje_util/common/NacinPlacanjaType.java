package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NacinPlacanjaType {
    Gotovina(0),
    Kartica(1),
    TransakcijskiRacun(2),
    Ostalo(3);

    private final int value;

    public static NacinPlacanjaType fromValue(int value) {
        for (NacinPlacanjaType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid NacinPlacanjaType: " + value);
    }
}