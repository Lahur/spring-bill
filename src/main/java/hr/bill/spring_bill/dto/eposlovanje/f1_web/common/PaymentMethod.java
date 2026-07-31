package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentMethod {
    Cash("Gotovina"),
    Card("Kartica"),
    Other("Ostalo"),
    BankTransfer("Transakcijski račun");

    private final String displayName;
}