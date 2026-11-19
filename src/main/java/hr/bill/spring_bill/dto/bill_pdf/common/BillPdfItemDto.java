package hr.bill.spring_bill.dto.bill_pdf.common;

import lombok.Builder;

@Builder
public record BillPdfItemDto(
        String name,
        String description,
        String basePrice
) {}
