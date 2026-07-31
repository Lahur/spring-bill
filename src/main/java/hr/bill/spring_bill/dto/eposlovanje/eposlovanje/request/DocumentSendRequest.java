package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record DocumentSendRequest(
        String document,
        String softwareId,
        Boolean sendAsEmail
) {}
