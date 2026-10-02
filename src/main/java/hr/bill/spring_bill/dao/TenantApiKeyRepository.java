package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.TenantApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantApiKeyRepository extends JpaRepository<TenantApiKeyEntity, UUID> {

    /** The current tenant's row; there's at most one per tenant. */
    Optional<TenantApiKeyEntity> findFirstBy();
}
