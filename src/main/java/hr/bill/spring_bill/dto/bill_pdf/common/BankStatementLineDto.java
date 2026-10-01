package hr.bill.spring_bill.dto.bill_pdf.common;

import lombok.Builder;

@Builder
public record BankStatementLineDto(
        Integer rowNumber,
        String bookingDate,
        String valueDate,
        String transactionReference,
        String counterpartyIban,
        String counterpartyName,
        String payerReference,
        String payeeReference,
        String description,
        String debitAmount,
        String creditAmount
) {}
