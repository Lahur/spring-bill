package hr.bill.spring_bill.dto.bill_pdf.common;

public record TaxSubtotalDto(
        String categoryId,
        String percent,
        String taxableAmount,
        String taxAmount,
        String taxExemptionReason
) {}
