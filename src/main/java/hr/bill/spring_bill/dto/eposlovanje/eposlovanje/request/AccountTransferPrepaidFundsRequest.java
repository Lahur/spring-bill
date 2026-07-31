package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record AccountTransferPrepaidFundsRequest(
        String destinationUsername,
        Double amount
) {}
