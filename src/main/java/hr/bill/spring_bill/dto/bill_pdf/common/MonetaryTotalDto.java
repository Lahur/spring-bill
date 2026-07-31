package hr.bill.spring_bill.dto.bill_pdf.common;

public record MonetaryTotalDto(
        String lineExtensionAmount,
        String taxExclusiveAmount,
        String taxInclusiveAmount,
        String prepaidAmount,
        String payableAmount
) {}
