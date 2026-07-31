package hr.bill.spring_bill.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "monthly_summary")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "month", nullable = false, unique = true)
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