package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity {

    /** The tenant id clients send in the X-Tenant-Id header; every other entity's tenant_id points here. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "oib", nullable = false)
    private String oib;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_zone", nullable = false)
    private String postalZone;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "contact_oib", nullable = false)
    private String contactOib;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "iban", nullable = false)
    private String iban;

    @OneToMany(mappedBy = "tenant")
    @Builder.Default
    private List<TenantPropertyEntity> properties = new ArrayList<>();
}
