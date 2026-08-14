package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record DocumentChangeStatusRequest(
        Integer status,
        String changedOn,
        String note,
        Double partialPaymentAmount
) {}
