package hr.bill.spring_bill.config.tenant;

import hr.bill.spring_bill.web.TenantController;
import hr.bill.spring_bill.web.TenantPropertyController;
import io.swagger.v3.oas.models.media.UUIDSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Documents the {@value TenantFilter#HEADER} header {@link TenantFilter} requires on every endpoint except
 * the admin tenant API ({@link TenantController}, {@link TenantPropertyController}), which takes the tenant from the path.
 */
@Configuration
public class TenantOpenApiConfig {

    private static final Set<Class<?>> PATH_TENANT_CONTROLLERS = Set.of(TenantController.class, TenantPropertyController.class);

    @Bean
    public OperationCustomizer tenantHeaderCustomizer() {
        return (operation, handlerMethod) -> PATH_TENANT_CONTROLLERS.contains(handlerMethod.getBeanType())
                ? operation
                : operation.addParametersItem(new HeaderParameter()
                        .name(TenantFilter.HEADER)
                        .required(true)
                        .description("Tenant the request is made for")
                        .schema(new UUIDSchema()));
    }
}
