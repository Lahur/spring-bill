package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

import lombok.Builder;

@Builder
public record PaymentInfo(
        String currency,
        Double amount,
        String payerName,
        String payerAddress,
        String payerZip,
        String payerCity,
        String payeeName,
        String payeeAddress,
        String payeeZip,
        String payeeCity,
        String payeeIBAN,
        String model,
        String referenceNumber,
        String description
) {}
