package hr.bill.spring_bill.clients.bill_pdf;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class BillPdfClientConfig {

    @Bean
    public RequestInterceptor billPdfTenantInterceptor() {
        return template -> template.header("x-tenant-id", "tehnomodus");
    }
}
