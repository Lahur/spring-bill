package hr.bill.spring_bill.web;

import com.fasterxml.jackson.databind.JsonNode;
import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.model.enums.TenantPropety;
import hr.bill.spring_bill.service.BankStatementScheduler;
import hr.bill.spring_bill.service.TenantPropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imports statements from the eposlovanje-mock's /ais routes. The mock generates transactions per
 * account and day (seeded, so the same day always returns the same data), relative to the current
 * UTC date, so expectations are read from the mock instead of hardcoded. Each test uses its own date
 * range because imported days are skipped on later imports and the database is shared between tests.
 */
class BankStatementFetchIT extends AbstractIntegrationTest {

    /** The account of the requisition the mock seeds as already linked. */
    private static final String MOCK_ACCOUNT_ID = "22222222-2222-4222-8222-222222222222";

    private static final String RECIPIENT_EMAIL = "statements@example.com";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private PosTransactionRepository posTransactionRepository;

    @Autowired
    private CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    @Autowired
    private BankStatementScheduler bankStatementScheduler;

    @Autowired
    private TenantPropertyService tenantPropertyService;

    @Test
    void fetchingSavesOneStatementPerBookingDayWithTheMockTransactions() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate dateFrom = today.minusDays(14);
        LocalDate dateTill = today.minusDays(8);
        Map<LocalDate, List<JsonNode>> expectedByDay = mockBookedTransactionsByDay(dateFrom, dateTill);
        List<JsonNode> expected = expectedByDay.values().stream().flatMap(List::stream).toList();
        assertThat(expected).as("mock should generate transactions for a full week").isNotEmpty();
        long posCountBefore = posTransactionRepository.count();
        long cashWithdrawalCountBefore = cashWithdrawalBalanceRepository.count();

        BankStatementResponse[] imported = fetch(dateFrom, dateTill);

        assertThat(imported).extracting(BankStatementResponse::periodFrom)
                .containsExactlyElementsOf(expectedByDay.keySet());
        for (BankStatementResponse statement : imported) {
            LocalDate day = statement.periodFrom();
            List<JsonNode> dayExpected = expectedByDay.get(day);
            assertThat(statement.periodTo()).isEqualTo(day);
            assertThat(statement.statementId())
                    .isEqualTo(TEST_TENANT.iban() + "-" + day.format(DateTimeFormatter.BASIC_ISO_DATE));

            List<BankTransactionEntity> saved =
                    bankTransactionRepository.findAllByBankStatement_IdOrderByTransactionDateAsc(statement.id());
            assertThat(saved).extracting(BankTransactionEntity::getTransactionReference)
                    .containsExactlyInAnyOrderElementsOf(dayExpected.stream().map(tx -> tx.get("transactionId").asText()).toList());
            for (BankTransactionEntity tx : saved) {
                JsonNode source = dayExpected.stream()
                        .filter(e -> e.get("transactionId").asText().equals(tx.getTransactionReference()))
                        .findFirst().orElseThrow();
                BigDecimal amount = new BigDecimal(source.at("/transactionAmount/amount").asText());
                assertThat(tx.getAmount()).isEqualByComparingTo(amount.abs());
                assertThat(tx.getCreditDebitIndicator())
                        .isEqualTo(amount.signum() < 0 ? CreditDebitIndicator.DBIT : CreditDebitIndicator.CRDT);
                assertThat(tx.getTransactionType()).isEqualTo(expectedType(source));
                assertThat(tx.getCounterpartyName()).isNotBlank();
            }

            BankStatementEntity entity = bankStatementRepository.findById(statement.id()).orElseThrow();
            List<BigDecimal> amounts = dayExpected.stream()
                    .map(tx -> new BigDecimal(tx.at("/transactionAmount/amount").asText()))
                    .toList();
            assertThat(entity.getCreditCount()).isEqualTo((int) amounts.stream().filter(a -> a.signum() > 0).count());
            assertThat(entity.getDebitCount()).isEqualTo((int) amounts.stream().filter(a -> a.signum() < 0).count());
            assertThat(entity.getCreditSum()).isEqualByComparingTo(
                    amounts.stream().filter(a -> a.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add));
            assertThat(entity.getDebitSum()).isEqualByComparingTo(
                    amounts.stream().filter(a -> a.signum() < 0).map(BigDecimal::abs).reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        assertThat(posTransactionRepository.count()).isEqualTo(posCountBefore
                + expected.stream().filter(tx -> expectedType(tx) == BankTransactionType.POS_PAY).count());
        assertThat(cashWithdrawalBalanceRepository.count()).isEqualTo(cashWithdrawalCountBefore
                + expected.stream().filter(tx -> expectedType(tx) == BankTransactionType.BANK_WITHDRAWAL).count());
    }

    @Test
    void fetchingTheSameDaysAgainIsSkipped() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate dateFrom = today.minusDays(35);
        LocalDate dateTill = today.minusDays(29);

        BankStatementResponse[] first = fetch(dateFrom, dateTill);
        assertThat(first).isNotEmpty();
        long transactionCountAfterFirst = bankTransactionRepository.count();

        BankStatementResponse[] second = fetch(dateFrom, dateTill);

        assertThat(second).isEmpty();
        assertThat(bankTransactionRepository.count()).isEqualTo(transactionCountAfterFirst);
    }

    @Test
    void fetchingUpToTodayIsRejected() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(post("/bank-statement/fetch")
                        .param("dateFrom", today.minusDays(3).toString())
                        .param("dateTill", today.toString())
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchingAnInvertedRangeIsRejected() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(post("/bank-statement/fetch")
                        .param("dateFrom", today.minusDays(2).toString())
                        .param("dateTill", today.minusDays(5).toString())
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedFetchIsRejected() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        mockMvc.perform(post("/bank-statement/fetch")
                        .param("dateFrom", yesterday.toString())
                        .param("dateTill", yesterday.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void schedulerImportsYesterdayAndMailsItWhenEnabled() throws Exception {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Europe/Zagreb")).minusDays(1);
        // The mock only has a day's transactions as booked once that UTC day is over; weekends often have none.
        boolean mockHasYesterday = !mockBookedTransactionsByDay(yesterday, yesterday).isEmpty();
        String statementId = TEST_TENANT.iban() + "-" + yesterday.format(DateTimeFormatter.BASIC_ISO_DATE);
        long mailCountBefore = mailhogMessagesTo(RECIPIENT_EMAIL);

        ReflectionTestUtils.setField(bankStatementScheduler, "mailEnabled", true);
        tenantPropertyService.save(TenantPropety.STATEMENT_MAIL_TO, RECIPIENT_EMAIL);
        try {
            bankStatementScheduler.importPreviousDay();
        } finally {
            ReflectionTestUtils.setField(bankStatementScheduler, "mailEnabled", false);
            tenantPropertyService.save(TenantPropety.STATEMENT_MAIL_TO, null);
        }

        assertThat(bankStatementRepository.existsByStatementId(statementId)).isEqualTo(mockHasYesterday);
        assertThat(mailhogMessagesTo(RECIPIENT_EMAIL)).isEqualTo(mailCountBefore + (mockHasYesterday ? 1 : 0));
    }

    private BankStatementResponse[] fetch(LocalDate dateFrom, LocalDate dateTill) throws Exception {
        return objectMapper.readValue(mockMvc.perform(post("/bank-statement/fetch")
                        .param("dateFrom", dateFrom.toString())
                        .param("dateTill", dateTill.toString())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankStatementResponse[].class);
    }

    private static BankTransactionType expectedType(JsonNode tx) {
        String text = tx.path("remittanceInformationUnstructured").asText("");
        if (text.startsWith("ISPLATA BANKOMAT")) {
            return BankTransactionType.BANK_WITHDRAWAL;
        }
        if (text.startsWith("POS KUPOVINA")) {
            return BankTransactionType.POS_PAY;
        }
        return BankTransactionType.TRANSACTION;
    }

    private Map<LocalDate, List<JsonNode>> mockBookedTransactionsByDay(LocalDate dateFrom, LocalDate dateTill)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(eposlovanjeMockBaseUrl() + "/ais/api/v2/accounts/"
                        + MOCK_ACCOUNT_ID + "/transactions?date_from=" + dateFrom + "&date_to=" + dateTill))
                .header("X-Api-Key", "test-ais-key")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        List<JsonNode> booked = new ArrayList<>();
        objectMapper.readTree(response.body()).at("/transactions/booked").forEach(booked::add);
        return booked.stream().collect(Collectors.groupingBy(
                tx -> LocalDate.parse(tx.get("bookingDate").asText()), TreeMap::new, Collectors.toList()));
    }
}
