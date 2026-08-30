package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.TenantPropertyEntity;
import hr.bill.spring_bill.model.enums.TenantPropety;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TenantPropertyRepository extends JpaRepository<TenantPropertyEntity, UUID> {

    List<TenantPropertyEntity> findByPropertyIn(Collection<TenantPropety> properties);
}
