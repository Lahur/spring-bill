package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {

    @Query("SELECT t.id FROM TenantEntity t ORDER BY t.id")
    List<UUID> findAllIds();
}
