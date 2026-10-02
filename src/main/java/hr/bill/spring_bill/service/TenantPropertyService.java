package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.TenantPropertyRepository;
import hr.bill.spring_bill.model.TenantPropertyEntity;
import hr.bill.spring_bill.model.enums.TenantPropety;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantPropertyService {

    private final TenantPropertyRepository tenantPropertyRepository;

    /** The current tenant's value for {@code property}, if set and not blank. */
    public Optional<String> find(TenantPropety property) {
        log.debug("Fetching tenant property {}", property);
        return tenantPropertyRepository.findByProperty(property)
                .map(TenantPropertyEntity::getValue)
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    /** Sets {@code property} for the current tenant; a null or blank {@code value} removes it. */
    @Transactional
    public void save(TenantPropety property, String value) {
        Optional<TenantPropertyEntity> existing = tenantPropertyRepository.findByProperty(property);
        if (value == null || value.isBlank()) {
            log.debug("Removing tenant property {}", property);
            existing.ifPresent(tenantPropertyRepository::delete);
            return;
        }
        log.debug("Saving tenant property {}", property);
        TenantPropertyEntity entity = existing.orElseGet(() -> TenantPropertyEntity.builder().property(property).build());
        entity.setValue(value.trim());
        tenantPropertyRepository.save(entity);
    }
}
