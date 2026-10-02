package hr.bill.spring_bill.clients.f1_web;

import feign.RequestInterceptor;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import hr.bill.spring_bill.service.TenantApiKeyService;
import org.springframework.context.annotation.Bean;

public class F1WebClientConfig {

    @Bean
    public RequestInterceptor f1WebAuthInterceptor(TenantApiKeyService tenantApiKeyService) {
        return template -> template.header("Authorization", tenantApiKeyService.require("F1 web", TenantApiKeyEntity::getF1WebApiKey));
    }
}
