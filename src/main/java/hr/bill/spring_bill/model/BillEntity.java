package hr.bill.spring_bill.model;

import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "system_id", "bill_type"}))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BillEntity extends TenantScopedEntity {

    @Column(name = "system_id", nullable = false)
    private Long systemId;

    @Column(name = "full_bill_id", nullable = false)
    private String fullBillId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_oib", length = 11)
    private String clientOib;

    @Column(name = "bill_date", nullable = false)
    private LocalDateTime billDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", length = 50)
    private BillDocumentStatus documentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type", nullable = false, length = 50)
    private BillType billType;

    @Builder.Default
    @Column(name = "sent_count", nullable = false)
    private int sentCount = 0;

    @Column(name = "payment_reference", length = 50)
    private String paymentReference;
}