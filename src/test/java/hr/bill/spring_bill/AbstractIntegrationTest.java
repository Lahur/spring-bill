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
import org.testcontainers.utility.DockerImageName;

import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.BillMaintenanceScheduler;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Base for full-{@code @SpringBootTest} integration tests that drive the app through its actual
 * HTTP endpoints (via {@link MockMvc}, security filter chain included — see
 * {@link #jwt() jwt()}), rather than calling service beans directly. So each test class only
 * declares what makes it different, this wires up:
 * <ul>
 *     <li>a throwaway Postgres (Testcontainers), auto-connected via {@link ServiceConnection} —
 *     Flyway migrations run against it for real;</li>
 *     <li>the {@code eposlovanje-mock} container, with {@code bill.eposlovanje/pondi/f1-web.base-url}
 *     pointed at it — its {@code /eposlovanje} and {@code /pondi} routes proxy straight through to
 *     Eposlovanje's test/sandbox environment with baked-in credentials; {@code /f1-web} is a real
 *     in-memory fake, not proxied;</li>
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
 *
 * <p>Containers are started once in a static initializer and never explicitly stopped (Ryuk reaps
 * them at JVM exit) rather than via {@code @Testcontainers}/{@code @Container} — those annotations
 * manage start/stop per <i>declaring</i> test class, so with several subclasses of this class in
 * the same JVM run, one class finishing its tests would stop the (shared, static) container out
 * from under the others, sending them "connection refused". This is the standard Testcontainers
 * "singleton container" pattern for exactly that scenario.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final DockerImageName EPOSLOVANJE_MOCK_IMAGE = DockerImageName.parse(
            "88a3be32-7c1b-485a-9715-09f7f654160a.europe.registry.cloudfleet.dev/eposlovanje-mock:latest");

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.4");

    static final GenericContainer<?> eposlovanjeMock = new GenericContainer<>(EPOSLOVANJE_MOCK_IMAGE)
            .withExposedPorts(8082);

    static {
        postgres.start();
        eposlovanjeMock.start();
    }

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

    @Autowired
    protected BankStatementRepository bankStatementRepository;

    @Autowired
    protected BankTransactionRepository bankTransactionRepository;

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

    /** Seeds a minimal bank_statement + bank_transaction row directly (bypassing the whole
     * CAMT-import pipeline), for tests that need a real {@link BankTransactionEntity} to hang a
     * downstream entity (POS transaction, cash withdrawal balance, ...) off of via its FK. */
    protected BankTransactionEntity seedBankTransaction(BigDecimal amount, CreditDebitIndicator indicator) {
        BankStatementEntity statement = bankStatementRepository.save(BankStatementEntity.builder()
                .statementId(UUID.randomUUID().toString())
                .iban("HR8823600001109999001")
                .build());
        return bankTransactionRepository.save(BankTransactionEntity.builder()
                .bankStatement(statement)
                .amount(amount)
                .creditDebitIndicator(indicator)
                .transactionType(BankTransactionType.TRANSACTION)
                .build());
    }
}
