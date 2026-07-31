package hr.bill.spring_bill.dto.bill_pdf.common;

public record IncomingSupplierDto(
        String name,
        String oib,
        String street,
        String city,
        String postalZone,
        String countryCode,
        String contactName,
        String email
) {}
