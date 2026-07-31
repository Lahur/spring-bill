package hr.bill.spring_bill.dto.eposlovanje.f1_web.request;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.TaxRate;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.UnitOfMeasure;
import lombok.Builder;

@Builder
public record CreateReceiptItemDto(
        String name,
        String description,
        Double quantity,
        Double unitPrice,
        TaxRate taxRate,
        UnitOfMeasure unitOfMeasure,
        Double discountAmount,
        Double discountPercent
) {}
