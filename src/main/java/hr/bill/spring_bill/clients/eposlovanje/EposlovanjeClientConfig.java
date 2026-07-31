package hr.bill.spring_bill.clients.eposlovanje;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class EposlovanjeClientConfig {

    @Value("${bill.eposlovanje.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor eposlovanjeAuthInterceptor() {
        return template -> template.header("Authorization", apiKey);
    }
}
