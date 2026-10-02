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

    @Transactional
    public TenantEntity save(TenantDto dto) {
        UUID tenantId = TenantContext.require();
        log.debug("Saving tenant {} with OIB {}", tenantId, dto.oib());
        TenantEntity entity = tenantRepository.findById(tenantId)
                .orElseGet(() -> TenantEntity.builder().id(tenantId).build());
        tenantMapper.updateTenantEntity(dto, entity);
        TenantEntity saved = tenantRepository.save(entity);
        log.debug("Saved tenant {}", saved.getId());
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
