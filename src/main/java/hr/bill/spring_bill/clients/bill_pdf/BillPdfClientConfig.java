package hr.bill.spring_bill.clients.bill_pdf;

import feign.RequestInterceptor;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import hr.bill.spring_bill.service.TenantApiKeyService;
import org.springframework.context.annotation.Bean;

public class BillPdfClientConfig {

    // Optional per tenant: without one, hub-bill renders its bundled templates. Also what the ITs rely
    // on — hub-bill there runs with no tenant database, so any x-tenant-id header makes it 400 on every
    // tenant-customizable template (see AbstractIntegrationTest).
    @Bean
    public RequestInterceptor billPdfTenantInterceptor(TenantApiKeyService tenantApiKeyService) {
        return template -> tenantApiKeyService.find(TenantApiKeyEntity::getHubTenantId)
                .ifPresent(hubTenantId -> template.header("x-tenant-id", hubTenantId));
    }
}
