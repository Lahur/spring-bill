package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BankingTransactionType {
    Uplata(1),
    Isplata(2);

    private final int value;

    public static BankingTransactionType fromValue(int value) {
        for (BankingTransactionType t : values()) {
            if (t.value == value) return t;
        }
        throw new IllegalArgumentException("Invalid BankingTransactionType value: " + value);
    }
}