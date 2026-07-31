package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.request;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.FiscalizationEnvironmentType;
import lombok.Builder;

@Builder
public record FiscalizeDocumentRequestDTO(
        FiscalizationEnvironmentType environment,
        String certificate,
        String certificatePassword,
        FiscalizationRequest fiscalizationData
) {}
