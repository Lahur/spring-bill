package hr.bill.spring_bill.model;

import hr.bill.spring_bill.model.enums.TenantPropety;
import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tenant_property")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPropertyEntity extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private TenantEntity tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "property", nullable = false)
    private TenantPropety property;

    @Column(name = "value", nullable = false)
    private String value;

}
