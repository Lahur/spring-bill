package hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common;

import lombok.Builder;

@Builder
public record PaymentParty(
        String name,
        String address,
        String city,
        String postalZone
) {}