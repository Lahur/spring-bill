package hr.bill.spring_bill.dto.bill_pdf.request;

import hr.bill.spring_bill.dto.bill_pdf.common.*;
import lombok.Builder;

import java.util.List;

@Builder
public record IncomingInvoiceRequest(
        String invoiceId,
        String issueDate,
        String issueTime,
        String dueDate,
        String deliveryDate,
        String currencyCode,
        String invoiceTypeCode,
        Boolean copyIndicator,
        List<String> notes,
        IncomingSupplierDto supplier,
        IncomingCustomerDto customer,
        IncomingPaymentMeansDto paymentMeans,
        IncomingTaxTotalDto taxTotal,
        MonetaryTotalDto monetaryTotal,
        List<InvoiceLineDto> lines
) {}
