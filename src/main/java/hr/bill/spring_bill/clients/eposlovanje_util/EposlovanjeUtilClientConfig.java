package hr.bill.spring_bill.clients.eposlovanje_util;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class EposlovanjeUtilClientConfig {

    @Value("${bill.pondi.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor eposlovanjeUtilAuthInterceptor() {
        return template -> template.header("ApiKey", apiKey);
    }
}
