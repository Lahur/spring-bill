package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

public record F2Settings(
        Boolean automaticFiscalizeOutgoingInvoices,
        Boolean automaticFiscalizeIncomingInvoices
) {}
