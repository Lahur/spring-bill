package hr.bill.spring_bill.dto.eposlovanje.f1_web.common;

public record ReceiptItemDto(
        Integer id,
        String name,
        String description,
        Double quantity,
        Double unitPrice,
        TaxRate taxRate,
        UnitOfMeasure unitOfMeasure,
        Double discountAmount,
        Double discountPercent,
        Double totalPrice,
        Double taxAmount,
        Double totalPriceWithTax
) {}
