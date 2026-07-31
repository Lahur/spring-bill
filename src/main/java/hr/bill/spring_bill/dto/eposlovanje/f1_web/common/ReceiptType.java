package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReceiptType {
    Standard("Standardni"),
    Training("Testni"),
    Copy("Kopija"),
    CreditNote("Odobrenje");

    private final String displayName;
}
