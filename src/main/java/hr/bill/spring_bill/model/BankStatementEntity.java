package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_statement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "statement_id", nullable = false)
    private String statementId;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "iban", length = 34, nullable = false)
    private String iban;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_address")
    private String ownerAddress;

    @Column(name = "owner_oib", length = 20)
    private String ownerOib;

    @Column(name = "bank_bic", length = 20)
    private String bankBic;

    @Column(name = "bank_oib", length = 20)
    private String bankOib;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "credit_count")
    private Integer creditCount;

    @Column(name = "credit_sum", precision = 19, scale = 2)
    private BigDecimal creditSum;

    @Column(name = "debit_count")
    private Integer debitCount;

    @Column(name = "debit_sum", precision = 19, scale = 2)
    private BigDecimal debitSum;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;
}
