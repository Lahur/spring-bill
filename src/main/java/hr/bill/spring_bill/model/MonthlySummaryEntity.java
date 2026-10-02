package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "monthly_summary", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "month"}))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryEntity extends TenantScopedEntity {

    @Column(name = "month", nullable = false)
    private LocalDate month;

    @Column(name = "sales_paid_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal salesPaidTotal;

    @Column(name = "sales_unpaid_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal salesUnpaidTotal;

    @Column(name = "purchases_paid_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasesPaidTotal;

    @Column(name = "purchases_unpaid_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasesUnpaidTotal;
}