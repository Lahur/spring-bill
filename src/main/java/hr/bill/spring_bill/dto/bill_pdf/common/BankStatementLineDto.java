package hr.bill.spring_bill.dto.bill_pdf.common;

import lombok.Builder;

@Builder
public record BankStatementLineDto(
        Integer rowNumber,
        String bookingDate,
        String valueDate,
        String entryReference,
        String transactionReference,
        String counterpartyIban,
        String counterpartyName,
        String counterpartyAddress,
        String payerReference,
        String payeeReference,
        String description,
        String debitAmount,
        String creditAmount
) {}
