package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts_statement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "bill_path")
    private String billPath;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;

    @Builder.Default
    @ManyToMany(mappedBy = "accountsStatements", fetch = FetchType.LAZY)
    private Set<CashWithdrawalBalanceEntity> cashWithdrawalBalances = new HashSet<>();

}
