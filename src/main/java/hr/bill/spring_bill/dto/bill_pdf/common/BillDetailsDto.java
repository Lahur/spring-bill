package hr.bill.spring_bill.dto.bill_pdf.common;

import java.util.List;

public record BillDetailsDto(
        String billNumber,
        String billDate,
        Boolean copyIndicator,
        String invoiceTypeCode,
        String currencyCode,
        String dueDate,
        String periodStart,
        String periodEnd,
        String orderReferenceId,
        String customizationId,
        String profileId,
        String invoiceNote,
        OutgoingSupplierDto supplier,
        OutgoingCustomerDto customer,
        OutgoingPaymentMeansDto paymentMeans,
        List<InvoiceLineDto> lines,
        List<TaxSubtotalDto> taxSubtotals,
        String taxTotalAmount,
        MonetaryTotalDto monetary
) {}
