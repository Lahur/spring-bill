package hr.bill.spring_bill.config.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Binds the {@value #HEADER} request header to {@link TenantContext} for the whole request. Ordered
 * after Spring Security's filter chain (-100) so unauthenticated requests still get a 401, not a 400.
 * The admin tenant API ({@value #TENANT_API}) ignores the header: {@code POST /tenant} needs no tenant and
 * {@code /tenant/{tenantId}/**} binds the path id. Binding here, not in the controller, matters: open-in-view opens
 * the Hibernate session (and fixes its tenant) before the controller runs.
 */
@Component
@Order(0)
public class TenantFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Tenant-Id";

    private static final String TENANT_API = "/tenant";

    private static final List<String> EXCLUDED_PREFIXES = List.of("/actuator", "/swagger-ui", "/v3/api-docs", "/scalar");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = path(request);
        return path.equals(TENANT_API) || EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = path(request);
        if (path.startsWith(TENANT_API + "/")) {
            Optional<UUID> tenantId = parseTenantId(path.substring(TENANT_API.length() + 1).split("/", 2)[0]);
            if (tenantId.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant id in the path must be a UUID");
                return;
            }
            try (TenantContext.Scope ignored = TenantContext.bind(tenantId.get())) {
                chain.doFilter(request, response);
            }
            return;
        }
        Optional<UUID> tenantId = parseTenantId(request.getHeader(HEADER));
        if (tenantId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Header " + HEADER + " is required and must be a UUID");
            return;
        }
        try (TenantContext.Scope ignored = TenantContext.bind(tenantId.get())) {
            chain.doFilter(request, response);
        }
    }

    private static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private static Optional<UUID> parseTenantId(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            UUID tenantId = UUID.fromString(value.trim());
            return tenantId.equals(TenantIdentifierResolver.NO_TENANT) ? Optional.empty() : Optional.of(tenantId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
