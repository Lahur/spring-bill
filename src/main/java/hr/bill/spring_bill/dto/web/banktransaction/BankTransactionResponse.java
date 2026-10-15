package hr.bill.spring_bill.dto.web.banktransaction;

import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Schema(description = "A bank transaction imported as part of a bank statement")
public record BankTransactionResponse(

        @Schema(description = "Internal ID")
        UUID id,

        @Schema(description = "Internal ID of the bank statement the transaction belongs to")
        UUID bankStatementId,

        @Schema(description = "Transaction amount")
        BigDecimal amount,

        @Schema(description = "Whether the transaction credits (CRDT) or debits (DBIT) the account")
        CreditDebitIndicator creditDebitIndicator,

        @Schema(description = "IBAN of the sender")
        String senderIban,

        @Schema(description = "IBAN of the receiver")
        String receiverIban,

        @Schema(description = "Name of the counterparty")
        String counterpartyName,

        @Schema(description = "Payment reference")
        String reference,

        @Schema(description = "Payer reference")
        String payerReference,

        @Schema(description = "Transaction reference as assigned by the bank")
        String transactionReference,

        @Schema(description = "Additional remittance information")
        String additionalRemittanceInfo,

        @Schema(description = "Date and time of the transaction")
        LocalDateTime transactionDate,

        @Schema(description = "Value date of the transaction")
        LocalDate valueDate,

        @Schema(description = "Type of the transaction")
        BankTransactionType transactionType,

        @Schema(description = "System ID of the bill paid by this transaction, if matched")
        String billSystemId
) {
}
