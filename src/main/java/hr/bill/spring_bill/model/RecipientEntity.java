package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "recipient", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"}))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientEntity extends TenantScopedEntity {

    @Column(name = "email", nullable = false)
    private String email;
}
