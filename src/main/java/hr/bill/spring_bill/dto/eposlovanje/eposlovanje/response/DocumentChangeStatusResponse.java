package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatus;

public record DocumentChangeStatusResponse(Long id, DocumentStatus status, String changedOn) {}
