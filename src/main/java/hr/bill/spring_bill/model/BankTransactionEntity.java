package hr.bill.spring_bill.model;

import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "reference", length = 35)
    private String reference;

    @Column(name = "additional_remittance_info", length = 140)
    private String additionalRemittanceInfo;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private BankTransactionType transactionType;
}
