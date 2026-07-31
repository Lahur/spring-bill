package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FiscalStatus {
    Pending("Na čekanju"),
    Success("Uspješno"),
    Failed("Neuspješno"),
    RetryPending("Ponovni pokušaj na čekanju"),
    LateDelivery("Zakašnjela dostava"),
    NotRequired("Nije potrebno");

    private final String displayName;
}