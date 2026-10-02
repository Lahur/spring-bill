package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "pos_transaction")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PosTransactionEntity extends TenantScopedEntity {

    @Column(name = "bill_path")
    private String billPath;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_transaction_id", nullable = false)
    private BankTransactionEntity bankTransaction;
}
