package hr.bill.spring_bill.dto.bill_pdf.common;

import java.util.List;

public record IncomingTaxTotalDto(
        String taxAmount,
        List<TaxSubtotalDto> subtotals
) {}
