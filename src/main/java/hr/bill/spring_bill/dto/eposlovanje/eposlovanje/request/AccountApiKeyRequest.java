package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request;

import lombok.Builder;

@Builder
public record AccountApiKeyRequest(
        String username,
        String password,
        String vatId,
        String softwareId
) {}
