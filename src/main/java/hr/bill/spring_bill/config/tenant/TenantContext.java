package hr.bill.spring_bill.config.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Holds the tenant the current thread is working for. Set per request by {@link TenantFilter} and per
 * tenant by {@code TenantService#forEachTenant} for background work; read by Hibernate through
 * {@link TenantIdentifierResolver} to filter and stamp every {@code @TenantId} entity.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Optional<UUID> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static UUID require() {
        UUID tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant bound to the current thread");
        }
        return tenantId;
    }

    /** Binds {@code tenantId} until the returned scope is closed, which restores the previous tenant. */
    public static Scope bind(UUID tenantId) {
        UUID previous = CURRENT.get();
        CURRENT.set(tenantId);
        return () -> restore(previous);
    }

    /** Runs {@code action} as {@code tenantId}, restoring whatever tenant was bound before. */
    public static void runAs(UUID tenantId, Runnable action) {
        try (Scope ignored = bind(tenantId)) {
            action.run();
        }
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    private static void restore(UUID previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }
}
