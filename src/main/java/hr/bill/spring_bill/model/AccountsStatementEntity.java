package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts_statement")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsStatementEntity extends TenantScopedEntity {

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
