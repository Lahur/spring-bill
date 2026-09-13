package hr.bill.spring_bill.clients.bill_pdf;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BillPdfClientConfig {

    // Blank (rather than the real "tehnomodus" default) against a hub-bill instance with no
    // tenant database configured: it has no tenant row to look up, so any x-tenant-id header
    // makes it 400 on every tenant-customizable template (see AbstractIntegrationTest).
    @Bean
    public RequestInterceptor billPdfTenantInterceptor(@Value("${bill.hub-tenant-id:tehnomodus}") String tenantId) {
        return template -> {
            if (!tenantId.isBlank()) {
                template.header("x-tenant-id", tenantId);
            }
        };
    }
}
