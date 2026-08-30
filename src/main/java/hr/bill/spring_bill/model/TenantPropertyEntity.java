package hr.bill.spring_bill.model;

import hr.bill.spring_bill.model.enums.TenantPropety;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tenant_property")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "property", nullable = false)
    private TenantPropety property;

    @Column(name = "value", nullable = false)
    private String value;

}
