package hr.bill.spring_bill;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
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

import hr.bill.spring_bill.config.KeycloakRealmRoleConverter;
import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.config.tenant.TenantFilter;
import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.BillMaintenanceScheduler;
import hr.bill.spring_bill.dto.web.tenant.TenantApiKeyRequest;
import hr.bill.spring_bill.service.TenantApiKeyService;
import hr.bill.spring_bill.service.TenantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final DockerImageName EPOSLOVANJE_MOCK_IMAGE = DockerImageName.parse(
            "88a3be32-7c1b-485a-9715-09f7f654160a.europe.registry.cloudfleet.dev/eposlovanje-mock:latest");

    private static final DockerImageName HUB_BILL_IMAGE = DockerImageName.parse(
            "88a3be32-7c1b-485a-9715-09f7f654160a.europe.registry.cloudfleet.dev/hub-bill:latest");

    private static final DockerImageName MAIL_BILL_IMAGE = DockerImageName.parse(
            "88a3be32-7c1b-485a-9715-09f7f654160a.europe.registry.cloudfleet.dev/mail-bill-test:latest-test");

    private static final int MAIL_BILL_APP_PORT = 8081;

    private static final int MAILHOG_API_PORT = 8025;

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.4");

    static final GenericContainer<?> eposlovanjeMock = new GenericContainer<>(EPOSLOVANJE_MOCK_IMAGE)
            .withExposedPorts(8082);

    static final GenericContainer<?> hubBill = new GenericContainer<>(HUB_BILL_IMAGE)
            .withExposedPorts(3000);

    static final GenericContainer<?> mailBill = new GenericContainer<>(MAIL_BILL_IMAGE)
            .withExposedPorts(MAIL_BILL_APP_PORT, MAILHOG_API_PORT);

    static {
        postgres.start();
        eposlovanjeMock.start();
        hubBill.start();
        mailBill.start();
    }

    protected static String eposlovanjeMockBaseUrl() {
        return "http://" + eposlovanjeMock.getHost() + ":" + eposlovanjeMock.getMappedPort(8082);
    }

    protected static String hubBillBaseUrl() {
        return "http://" + hubBill.getHost() + ":" + hubBill.getMappedPort(3000);
    }

    protected static String mailBillBaseUrl() {
        return "http://" + mailBill.getHost() + ":" + mailBill.getMappedPort(MAIL_BILL_APP_PORT);
    }

    private static String mailhogApiBaseUrl() {
        return "http://" + mailBill.getHost() + ":" + mailBill.getMappedPort(MAILHOG_API_PORT);
    }

    @DynamicPropertySource
    static void externalClientProperties(DynamicPropertyRegistry registry) {
        registry.add("bill.eposlovanje.base-url", () -> eposlovanjeMockBaseUrl() + "/eposlovanje");
        registry.add("bill.pondi.base-url", () -> eposlovanjeMockBaseUrl() + "/pondi");
        registry.add("bill.f1-web.base-url", () -> eposlovanjeMockBaseUrl() + "/f1-web");
        registry.add("bill.ais-eposlovanje.base-url", () -> eposlovanjeMockBaseUrl() + "/ais");
        registry.add("bill.hub-url", AbstractIntegrationTest::hubBillBaseUrl);
        registry.add("bill.mail-bill.base-url", AbstractIntegrationTest::mailBillBaseUrl);
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

    @Autowired
    protected TenantService tenantService;

    @Autowired
    protected TenantApiKeyService tenantApiKeyService;

    protected static final UUID TEST_TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    protected static final TenantDto TEST_TENANT = TenantDto.builder()
            // Must be the real OIB the eposlovanje-mock sandbox account is registered under: on every UBL
            // invoice send, Eposlovanje checks this against the sender identity tied to the mock's baked-in
            // API key ("OIB pošiljatelja dokumenta (...) mora odgovarati
            // Invoice/AccountingSupplierParty/Party/EndpointID elementu (...)") — a placeholder OIB, even a
            // checksum-valid one, gets rejected with a 400.
            .oib("31728187872")
            .name("Test Supplier d.o.o.")
            .street("Test Street 1")
            .city("Zagreb")
            .postalZone("10000")
            .countryCode("HR")
            // Just needs to be a valid OIB for the contact/operator field on receipts and UBL sellerContact
            // (F1OutgoingStrategy/F2OutgoingStrategy/F2ReportStrategy) — unlike oib above, this isn't
            // checked against the sandbox account's registered identity, so it's deliberately different
            // here to reflect that it represents a distinct person, not the company itself.
            .contactOib("73660371074")
            .contactName("Test Contact")
            .phone("+385000000000")
            .email("test-supplier@example.com")
            // Must match the account IBAN baked into src/test/resources/camt/bank-statement-*.xml, so
            // BankStatementImportServiceIT's fixture entries are recognized as remote-match candidates.
            .iban("HR8823600001109999001")
            .build();

    private TenantContext.Scope tenantScope;

    /** Binds TEST_TENANT_ID on the test thread so repositories used directly by tests see its rows. */
    @BeforeEach
    void bindAndSeedTenant() {
        tenantScope = TenantContext.bind(TEST_TENANT_ID);
        tenantService.save(TEST_TENANT);
        // The eposlovanje-mock ignores these and injects its own real keys, but every Feign call
        // still requires the tenant to have one.
        tenantApiKeyService.save(TenantApiKeyRequest.builder()
                .eposlovanjeApiKey("test-eposlovanje-key")
                .f1WebApiKey("test-f1-web-key")
                .pondiApiKey("test-pondi-key")
                .aisEposlovanjeApiKey("test-ais-key")
                // No hubTenantId: hub-bill here runs with no DB_HOST, so it has no tenant table and a
                // real x-tenant-id 400s (see BillPdfClientConfig); without it hub-bill uses its bundled templates.
                .build());
    }

    @AfterEach
    void unbindTenant() {
        tenantScope.close();
    }

    protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /** An authenticated request for TEST_TENANT_ID. */
    protected static RequestPostProcessor jwt() {
        return jwtFor(TEST_TENANT_ID);
    }

    /** An authenticated request for {@code tenantId}. */
    protected static RequestPostProcessor jwtFor(UUID tenantId) {
        return request -> {
            request.addHeader(TenantFilter.HEADER, tenantId.toString());
            return SecurityMockMvcRequestPostProcessors.jwt().postProcessRequest(request);
        };
    }

    /** An authenticated request carrying the ADMIN realm role, with no tenant header (the tenant API takes it from the path). */
    protected static RequestPostProcessor adminJwt() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ADMIN"))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();


    protected long mailhogMessagesTo(String recipientEmail) throws IOException, InterruptedException {
        return mailhogSearch(recipientEmail).total();
    }


    protected MailhogMessage mailhogLatestMessageTo(String recipientEmail) throws IOException, InterruptedException {
        return mailhogSearch(recipientEmail).items().stream()
                .max(Comparator.comparing(MailhogItem::created))
                .map(MailhogItem::toMailhogMessage)
                .orElseThrow(() -> new AssertionError("No mailhog message found for recipient " + recipientEmail));
    }

    private MailhogSearchResult mailhogSearch(String recipientEmail) throws IOException, InterruptedException {
        String url = mailhogApiBaseUrl() + "/api/v2/search?kind=to&query="
                + URLEncoder.encode(recipientEmail, StandardCharsets.UTF_8);
        HttpResponse<String> response = HTTP_CLIENT.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
        return MAILHOG_OBJECT_MAPPER.readValue(response.body(), MailhogSearchResult.class);
    }

    /** A parsed Mailhog message, independent of Mailhog's raw MIME-part JSON shape. */
    public record MailhogMessage(String from, List<String> to, String subject, String body,
                                  List<MailhogAttachment> attachments) {
    }

    public record MailhogAttachment(String fileName, String contentType, int sizeBytes) {
    }

    // Mailhog v2 API JSON shape (https://github.com/mailhog/MailHog/blob/master/docs/APIv2/swagger-2.0.yaml)
    // uses PascalCase field names ("Total", "From", "Content", ...); kept private since callers only ever
    // see the flattened MailhogMessage/MailhogAttachment above.
    private static final ObjectMapper MAILHOG_OBJECT_MAPPER = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private record MailhogSearchResult(long total, List<MailhogItem> items) {

        private MailhogSearchResult {
            items = items == null ? List.of() : items;
        }
    }

    private record MailhogMailbox(String mailbox, String domain) {
        String address() {
            return mailbox + "@" + domain;
        }
    }

    private record MailhogPart(Map<String, List<String>> headers, String body) {

        private MailhogPart {
            headers = headers == null ? Map.of() : headers;
            body = body == null ? "" : body;
        }

        private String header(String name) {
            List<String> values = headers.get(name);
            return values == null || values.isEmpty() ? null : values.get(0);
        }

        private boolean isAttachment() {
            String disposition = header("Content-Disposition");
            return disposition != null && disposition.toLowerCase().startsWith("attachment");
        }

        private String attachmentFileName() {
            String disposition = header("Content-Disposition");
            if (disposition == null) return null;
            Matcher matcher = Pattern.compile("filename=\"?([^\";]+)\"?").matcher(disposition);
            return matcher.find() ? matcher.group(1) : null;
        }

        private String contentType() {
            String contentType = header("Content-Type");
            return contentType == null ? null : contentType.split(";")[0].trim();
        }

        private byte[] decodedBody() {
            String encoding = header("Content-Transfer-Encoding");
            if (encoding != null && encoding.equalsIgnoreCase("base64")) {
                return Base64.getMimeDecoder().decode(body.replaceAll("\\s", ""));
            }
            return body.getBytes(StandardCharsets.UTF_8);
        }
    }

    private record MailhogMime(List<MailhogPart> parts) {

        private MailhogMime {
            parts = parts == null ? List.of() : parts;
        }
    }

    private record MailhogItem(String id, MailhogMailbox from, List<MailhogMailbox> to, MailhogPart content,
                                MailhogMime mime, String created) {

        private MailhogMessage toMailhogMessage() {
            String subject = content.header("Subject");
            List<MailhogPart> parts = mime == null || mime.parts().isEmpty() ? List.of(content) : mime.parts();
            String body = parts.stream()
                    .filter(part -> !part.isAttachment())
                    .findFirst()
                    .map(part -> new String(part.decodedBody(), StandardCharsets.UTF_8))
                    .orElse(null);
            List<MailhogAttachment> attachments = parts.stream()
                    .filter(MailhogPart::isAttachment)
                    .map(part -> new MailhogAttachment(part.attachmentFileName(), part.contentType(),
                            part.decodedBody().length))
                    .toList();
            return new MailhogMessage(from.address(), to.stream().map(MailhogMailbox::address).toList(),
                    subject, body, attachments);
        }
    }

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
