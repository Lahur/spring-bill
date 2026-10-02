package hr.bill.spring_bill.clients.eposlovanje;

import feign.RequestInterceptor;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import hr.bill.spring_bill.service.TenantApiKeyService;
import org.springframework.context.annotation.Bean;

public class EposlovanjeClientConfig {

    @Bean
    public RequestInterceptor eposlovanjeAuthInterceptor(TenantApiKeyService tenantApiKeyService) {
        return template -> template.header("Authorization", tenantApiKeyService.require("eposlovanje", TenantApiKeyEntity::getEposlovanjeApiKey));
    }
}
