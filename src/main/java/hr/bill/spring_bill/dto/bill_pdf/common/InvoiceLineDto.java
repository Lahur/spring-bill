package hr.bill.spring_bill.dto.bill_pdf.common;

public record InvoiceLineDto(
        Integer id,
        String name,
        String description,
        String classificationCode,
        String quantity,
        String unitCode,
        String unitPrice,
        String lineExtensionAmount,
        String vatCategory
) {}
