package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatus;
import lombok.Builder;

@Builder
public record DocumentChangeStatusRequest(
        DocumentStatus status,
        String changedOn,
        String note,
        Double partialPaymentAmount
) {}
