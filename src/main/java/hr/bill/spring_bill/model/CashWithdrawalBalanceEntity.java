package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cash_withdrawal_balance")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CashWithdrawalBalanceEntity extends TenantScopedEntity {

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;

    @Column(name = "disbursement_number")
    private Integer disbursementNumber;

    @Column(name = "deposit_number")
    private Integer depositNumber;

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
