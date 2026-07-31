package hr.bill.spring_bill.clients.f1_web;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class F1WebClientConfig {

    @Value("${bill.f1-web.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor f1WebAuthInterceptor() {
        return template -> template.header("Authorization", apiKey);
    }
}
