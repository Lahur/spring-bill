package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

public record TaxBreakdownDto(
        Double taxRate,
        String taxRateDisplay,
        Double baseAmount,
        Double taxAmount,
        Double totalAmount
) {}
