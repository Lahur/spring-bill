package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

import java.util.List;

public record FiscalizationReadinessDto(
        Boolean isReady,
        List<String> issues,
        List<String> warnings,
        Boolean isBusinessConfigured,
        Boolean isCertificateValid,
        Boolean hasItems,
        Boolean isAlreadyFiscalized
) {}
