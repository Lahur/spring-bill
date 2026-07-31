package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.request;

import lombok.Builder;

@Builder
public record SendSmsMessageDTO(String recipientPhoneNumber, String message) {}
