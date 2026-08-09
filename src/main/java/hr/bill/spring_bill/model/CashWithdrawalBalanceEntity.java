package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cash_withdrawal_balance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashWithdrawalBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_transaction_id", nullable = false)
    private BankTransactionEntity bankTransaction;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cash_withdrawal_accounts_statement",
            joinColumns = @JoinColumn(name = "cash_withdrawal_balance_id"),
            inverseJoinColumns = @JoinColumn(name = "accounts_statement_id")
    )
    private Set<AccountsStatementEntity> accountsStatements = new HashSet<>();

}
