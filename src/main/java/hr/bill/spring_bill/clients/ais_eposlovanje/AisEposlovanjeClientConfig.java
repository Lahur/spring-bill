package hr.bill.spring_bill.clients.ais_eposlovanje;

import feign.RequestInterceptor;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import hr.bill.spring_bill.service.TenantApiKeyService;
import org.springframework.context.annotation.Bean;

public class AisEposlovanjeClientConfig {

    @Bean
    public RequestInterceptor aisEposlovanjeAuthInterceptor(TenantApiKeyService tenantApiKeyService) {
        return template -> template.header("X-Api-Key", tenantApiKeyService.require("AIS eposlovanje", TenantApiKeyEntity::getAisEposlovanjeApiKey));
    }
}
