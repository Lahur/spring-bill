package hr.bill.spring_bill.config.tenant;

import hr.bill.spring_bill.web.TenantController;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Documents the {@value TenantFilter#HEADER} header {@link TenantFilter} requires on every endpoint except
 * {@link TenantController}'s, which take the tenant from the path.
 */
@Configuration
public class TenantOpenApiConfig {

    @Bean
    public OperationCustomizer tenantHeaderCustomizer() {
        return (operation, handlerMethod) -> handlerMethod.getBeanType() == TenantController.class
                ? operation
                : operation.addParametersItem(new HeaderParameter()
                        .name(TenantFilter.HEADER)
                        .required(true)
                        .description("Tenant the request is made for")
                        .schema(new UUIDSchema()));
    }
}
