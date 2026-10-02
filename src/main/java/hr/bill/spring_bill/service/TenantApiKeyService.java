package hr.bill.spring_bill.service;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.dao.TenantApiKeyRepository;
import hr.bill.spring_bill.dto.web.tenant.TenantApiKeyRequest;
import hr.bill.spring_bill.mapper.TenantApiKeyMapper;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantApiKeyService {

    private final TenantApiKeyRepository tenantApiKeyRepository;
    private final TenantApiKeyMapper tenantApiKeyMapper;

    /** The current tenant's key for {@code client}; used by the Feign auth interceptors on every call. */
    public String require(String client, Function<TenantApiKeyEntity, String> apiKey) {
        return tenantApiKeyRepository.findFirstBy()
                .map(apiKey)
                .filter(key -> !key.isBlank())
                .orElseThrow(() -> new IllegalStateException(
                        "Tenant " + TenantContext.require() + " has no " + client + " API key configured"));
    }

    /** The current tenant's {@code value}, if it has one; for optional settings that may be left blank. */
    public Optional<String> find(Function<TenantApiKeyEntity, String> value) {
        return tenantApiKeyRepository.findFirstBy()
                .map(value)
                .filter(v -> !v.isBlank());
    }

    @Transactional
    public void save(TenantApiKeyRequest request) {
        log.debug("Saving API keys for tenant {}", TenantContext.require());
        TenantApiKeyEntity entity = tenantApiKeyRepository.findFirstBy().orElseGet(TenantApiKeyEntity::new);
        tenantApiKeyMapper.updateTenantApiKeyEntity(request, entity);
        tenantApiKeyRepository.save(entity);
        log.debug("Saved API keys for tenant {}", TenantContext.require());
    }
}
