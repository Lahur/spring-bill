package hr.bill.spring_bill.dto.bill_pdf.common;

public record OutgoingCustomerDto(
        String name,
        String oib,
        String street,
        String city,
        String postalZone,
        String countryCode
) {}
