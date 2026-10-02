package hr.bill.spring_bill.model;

import hr.bill.spring_bill.config.ApiKeyEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tenant_api_key")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantApiKeyEntity extends TenantScopedEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private TenantEntity tenant;

    @Column(name = "eposlovanje_api_key")
    @Convert(converter = ApiKeyEncryptionConverter.class)
    private String eposlovanjeApiKey;

    @Column(name = "f1_web_api_key")
    @Convert(converter = ApiKeyEncryptionConverter.class)
    private String f1WebApiKey;

    @Column(name = "pondi_api_key")
    @Convert(converter = ApiKeyEncryptionConverter.class)
    private String pondiApiKey;

    @Column(name = "ais_eposlovanje_api_key")
    @Convert(converter = ApiKeyEncryptionConverter.class)
    private String aisEposlovanjeApiKey;

    /** Sent to hub-bill as x-tenant-id to pick the tenant's PDF templates; blank uses hub-bill's bundled ones. */
    @Column(name = "hub_tenant_id")
    private String hubTenantId;
}
