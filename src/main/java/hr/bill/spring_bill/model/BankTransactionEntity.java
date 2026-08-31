package hr.bill.spring_bill.model;

import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_statement_id", nullable = false)
    private BankStatementEntity bankStatement;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_debit_indicator", length = 10)
    private CreditDebitIndicator creditDebitIndicator;

    @Column(name = "sender_iban", length = 34)
    private String senderIban;

    @Column(name = "receiver_iban", length = 34)
    private String receiverIban;

    @Column(name = "counterparty_name", length = 140)
    private String counterpartyName;

    @Column(name = "counterparty_address", length = 210)
    private String counterpartyAddress;

    @Column(name = "reference", length = 35)
    private String reference;

    @Column(name = "payer_reference", length = 35)
    private String payerReference;

    @Column(name = "entry_reference", length = 64)
    private String entryReference;

    @Column(name = "transaction_reference", length = 64)
    private String transactionReference;

    @Column(name = "additional_remittance_info", length = 140)
    private String additionalRemittanceInfo;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private BankTransactionType transactionType;

    @Column(name = "bill_system_id")
    private String billSystemId;
}
