package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "pos_transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bill_path")
    private String billPath;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bank_transaction_id", nullable = false)
    private BankTransactionEntity bankTransaction;
}
