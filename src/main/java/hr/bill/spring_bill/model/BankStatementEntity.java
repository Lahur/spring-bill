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

    @Column(name = "iban", length = 34, nullable = false)
    private String iban;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
