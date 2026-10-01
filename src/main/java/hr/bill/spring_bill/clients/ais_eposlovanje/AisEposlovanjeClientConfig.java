package hr.bill.spring_bill.clients.ais_eposlovanje;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AisEposlovanjeClientConfig {

    @Value("${bill.ais-eposlovanje.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor aisEposlovanjeAuthInterceptor() {
        return template -> template.header("X-Api-Key", apiKey);
    }
}
