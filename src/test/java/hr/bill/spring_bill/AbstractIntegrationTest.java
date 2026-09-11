package hr.bill.spring_bill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import hr.bill.spring_bill.service.BillMaintenanceScheduler;

/**
 * Base for full-{@code @SpringBootTest} integration tests that drive the app through its actual
 * HTTP endpoints (via {@link MockMvc}, security filter chain included — see
 * {@link #jwt() jwt()}), rather than calling service beans directly. So each test class only
 * declares what makes it different, this wires up:
 * <ul>
 *     <li>a throwaway Postgres (Testcontainers), auto-connected via {@link ServiceConnection} —
 *     Flyway migrations run against it for real;</li>
 *     <li>the {@code eposlovanje-mock} container, with {@code bill.eposlovanje/pondi/f1-web.base-url}
 *     pointed at it — its {@code /eposlovanje} and {@code /pondi} routes proxy
 *     straight through to Eposlovanje's test/sandbox environment with baked-in credentials;
 *     {@code /f1-web} is a real in-memory fake, not proxied;</li>
 *     <li>the {@code test} profile, so {@code bill.*} config comes from
 *     {@code src/test/resources/application-test.yaml} instead of Vault;</li>
 *     <li>{@link MockMvc}, wired through the real {@code SecurityFilterChain} — requests need
 *     {@link #jwt()} to authenticate, since every endpoint but swagger/actuator requires it;</li>
 *     <li>two infrastructure mocks unrelated to what any individual test exercises:
 *     {@link JwtDecoder} (the real bean hits the OIDC issuer/Keycloak at context startup —
 *     {@link #jwt()} bypasses it per-request anyway, by injecting the authentication directly) and
 *     {@link BillMaintenanceScheduler} (its {@code @EventListener(ApplicationReadyEvent.class)}
 *     syncs every {@code BillStrategy} against its real upstream on every context startup).</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final DockerImageName EPOSLOVANJE_MOCK_IMAGE = DockerImageName.parse(
            "88a3be32-7c1b-485a-9715-09f7f654160a.europe.registry.cloudfleet.dev/eposlovanje-mock:latest");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.4");

    @Container
    static GenericContainer<?> eposlovanjeMock = new GenericContainer<>(EPOSLOVANJE_MOCK_IMAGE)
            .withExposedPorts(8082);

    protected static String eposlovanjeMockBaseUrl() {
        return "http://" + eposlovanjeMock.getHost() + ":" + eposlovanjeMock.getMappedPort(8082);
    }

    @DynamicPropertySource
    static void externalClientProperties(DynamicPropertyRegistry registry) {
        registry.add("bill.eposlovanje.base-url", () -> eposlovanjeMockBaseUrl() + "/eposlovanje");
        registry.add("bill.pondi.base-url", () -> eposlovanjeMockBaseUrl() + "/pondi");
        registry.add("bill.f1-web.base-url", () -> eposlovanjeMockBaseUrl() + "/f1-web");
    }

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected BillMaintenanceScheduler billMaintenanceScheduler;

    @Autowired
    protected MockMvc mockMvc;

    // Built locally rather than @Autowired: the app also registers an XmlMapper bean (for CAMT
    // parsing), and since XmlMapper extends ObjectMapper, autowiring plain ObjectMapper here is
    // ambiguous — this is purely for reading MockMvc's JSON response bodies in tests, so it isn't
    // worth depending on which bean wins.
    protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /** A request post-processor that authenticates the request as a JWT principal directly (no
     * real token, no round-trip through {@link #jwtDecoder}) — required on every call, since
     * {@code SecurityConfig} requires authentication for anything but swagger/actuator. */
    protected static RequestPostProcessor jwt() {
        return SecurityMockMvcRequestPostProcessors.jwt();
    }
}
