package hr.bill.spring_bill.service;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.dao.TenantRepository;
import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.TenantMapper;
import hr.bill.spring_bill.model.TenantEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    public TenantEntity get() {
        UUID tenantId = TenantContext.require();
        log.debug("Fetching tenant {}", tenantId);
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant " + tenantId + " is not configured"));
    }

    /** Creates a new tenant with a generated id; no tenant needs to be bound. */
    @Transactional
    public TenantEntity create(TenantDto dto) {
        log.debug("Creating tenant with OIB {}", dto.oib());
        TenantEntity entity = TenantEntity.builder().id(UUID.randomUUID()).build();
        tenantMapper.updateTenantEntity(dto, entity);
        TenantEntity saved = tenantRepository.save(entity);
        log.debug("Created tenant {}", saved.getId());
        return saved;
    }

    /** Replaces the current tenant's data; 404 if it doesn't exist. */
    @Transactional
    public TenantEntity update(TenantDto dto) {
        TenantEntity entity = get();
        log.debug("Updating tenant {} with OIB {}", entity.getId(), dto.oib());
        tenantMapper.updateTenantEntity(dto, entity);
        TenantEntity saved = tenantRepository.save(entity);
        log.debug("Updated tenant {}", saved.getId());
        return saved;
    }

    /**
     * Runs {@code job} once per configured tenant with that tenant bound, for work that isn't driven by
     * a request (schedulers, startup listeners). A failure for one tenant is logged and doesn't stop
     * the others.
     */
    public void forEachTenant(String jobName, Runnable job) {
        List<UUID> tenantIds = tenantRepository.findAllIds();
        log.info("Running {} for {} tenant(s)", jobName, tenantIds.size());
        for (UUID tenantId : tenantIds) {
            try {
                TenantContext.runAs(tenantId, job);
            } catch (RuntimeException e) {
                log.error("{} failed for tenant {}", jobName, tenantId, e);
            }
        }
    }
}
