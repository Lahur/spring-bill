package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response;

import java.util.UUID;

public record SmsMessageDTO(
        UUID id,
        String recipientPhoneNumber,
        String message,
        String insertedAt,
        Boolean sent,
        String sentAt,
        Boolean delivered,
        String deliveredAt
) {}
