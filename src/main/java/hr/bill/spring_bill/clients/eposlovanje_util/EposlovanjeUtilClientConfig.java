package hr.bill.spring_bill.clients.eposlovanje_util;

import feign.RequestInterceptor;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import hr.bill.spring_bill.service.TenantApiKeyService;
import org.springframework.context.annotation.Bean;

public class EposlovanjeUtilClientConfig {

    @Bean
    public RequestInterceptor eposlovanjeUtilAuthInterceptor(TenantApiKeyService tenantApiKeyService) {
        return template -> template.header("ApiKey", tenantApiKeyService.require("pondi", TenantApiKeyEntity::getPondiApiKey));
    }
}
