package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillDocumentKind;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillItemInfo;
import hr.bill.spring_bill.dto.web.bill.info.BillPaymentMethod;
import hr.bill.spring_bill.dto.web.bill.info.BillVatRate;
import hr.bill.spring_bill.dto.web.bill.info.BuyerInfo;
import hr.bill.spring_bill.dto.web.bill.info.ItemUnitOfMeasure;
import hr.bill.spring_bill.dto.web.bill.info.MainDataInfo;
import hr.bill.spring_bill.dto.web.bill.info.PaymentInfo;
import hr.bill.spring_bill.dto.web.bill.info.PriceInfo;
import hr.bill.spring_bill.dto.web.bill.info.SupplierInfo;
import hr.bill.spring_bill.model.enums.BillType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link F2ReportBillController} over HTTP. Unlike {@link F2BillControllerIT}, there's no
 * AMS check here (buyer details are self-declared — {@code ReportBillRequest} carries its own
 * buyerName/buyerStreet/buyerCity/buyerPostalZone, no Eposlovanje lookup), and {@code getBills}/
 * {@code getBillsFilter}/{@code getBillInfo}/{@code review} are all purely local — only
 * {@code createBill} (via {@code eposlovanjeClient.reportDocument}) and {@code cancel} touch the
 * sandbox. {@code createBill} still renders and embeds a PDF via {@link BillPdfClient}, pointed at
 * the real (DB-less) {@code hub-bill} container from {@link AbstractIntegrationTest} as everywhere
 * else. */
class F2ReportBillControllerIT extends AbstractIntegrationTest {

    // Eposlovanje matches a created document by "{billId}/1/1" among today's outgoing documents
    // when cancelling, so every bill created in this run needs its own never-before-used billId.
    private static final AtomicInteger NEXT_BILL_ID = new AtomicInteger((int) (System.currentTimeMillis() % 1_000_000) + 1);

    @Autowired
    private SupplierProperties supplierProperties;

    @Test
    void createsListsAndFetchesABill() throws Exception {
        int billId = NEXT_BILL_ID.getAndIncrement();
        Map<String, Object> requestBody = reportBillRequestBody(billId);
        BillResponse created = createBill(billId);

        // createBill's mapping (BillEntityMapper.toBillEntity(ReportBillRequest, BigDecimal, BillType))
        // is entirely local — buyer details are self-declared, no upstream lookup — so every field but
        // the DB-generated id is deterministic from the request itself.
        BillResponse expected = BillResponse.builder()
                .systemId((long) billId)
                .fullBillId(billId + "/1/1")
                .clientName((String) requestBody.get("buyerName"))
                .clientOib((String) requestBody.get("buyerOib"))
                .billDate(LocalDate.now().atTime(11, 0))
                .totalAmount(new BigDecimal("125.00"))
                .documentStatus(null)
                .billType(BillType.F2_REPORT)
                .sentCount(0)
                .build();
        assertThat(created)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expected);
        assertThat(created.id()).isNotNull();

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/f2-report").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(created.id());

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/f2-report/" + created.id()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        // getBillInfo is purely local (BillInfoEntity/BillItemEntity saved from our own generated UBL
        // invoice at creation time), so every field of it is derivable from the request/config —
        // buyer straight from the self-declared request fields (no AMS lookup here, unlike F2), and
        // note buyerOib/supplierOib here come from partyLegalEntity.companyId (bare, no "HR" prefix),
        // unlike F2BillControllerIT's buyerInfo/supplierInfo which read partyTaxScheme.companyId.
        MainDataInfo expectedMainDataInfo = new MainDataInfo(
                LocalDate.now(),
                null,
                LocalDate.now().plusDays(15),
                BillDocumentKind.CommercialInvoice,
                null,
                null,
                "EUR",
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now());
        BuyerInfo expectedBuyerInfo = new BuyerInfo(
                (String) requestBody.get("buyerName"),
                (String) requestBody.get("buyerOib"),
                (String) requestBody.get("buyerStreet"),
                (String) requestBody.get("buyerCity"),
                (String) requestBody.get("buyerPostalZone"));
        SupplierInfo expectedSupplierInfo = new SupplierInfo(
                supplierProperties.name(),
                supplierProperties.oib(),
                supplierProperties.street(),
                supplierProperties.city(),
                supplierProperties.postalZone(),
                supplierProperties.contactName(),
                supplierProperties.contactOib(),
                supplierProperties.email(),
                supplierProperties.phone());
        BillItemInfo expectedItemInfo = new BillItemInfo(
                (String) requestBody.get("billItemName"),
                (String) requestBody.get("billItemDescription"),
                BigDecimal.ONE, ItemUnitOfMeasure.H87,
                null, new BigDecimal("100.00"), new BigDecimal("125.00"), null,
                BillVatRate.Pdv25, null);
        PaymentInfo expectedPaymentInfo = new PaymentInfo(
                BillPaymentMethod.CreditTransfer,
                LocalDate.now().plusDays(15),
                supplierProperties.iban(),
                "HR00",
                billId + "-1-1",
                "račun " + billId + "/1/1");
        PriceInfo expectedPriceInfo = new PriceInfo(
                new BigDecimal("100.00"), new BigDecimal("25.00"), new BigDecimal("125.00"),
                BigDecimal.ZERO, new BigDecimal("125.00"));
        BillInfoResponse expectedInfo = new BillInfoResponse(expectedMainDataInfo, expectedBuyerInfo,
                expectedSupplierInfo, List.of(expectedItemInfo), expectedPaymentInfo, null, expectedPriceInfo);
        assertThat(info)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedInfo);
    }

    @Test
    void filtersBillsByDateRange() throws Exception {
        BillResponse created = createBill(NEXT_BILL_ID.getAndIncrement());

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("dateFrom", LocalDate.now().minusDays(1).toString());

        BillResponse[] filtered = objectMapper.readValue(mockMvc.perform(post("/bill/f2-report/filter")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(filtered).map(BillResponse::id)).contains(created.id());
    }

    @Test
    void reviewsABillWithoutPersistingIt() throws Exception {
        long countBefore = countBills();

        BillReviewResponse review = objectMapper.readValue(mockMvc.perform(post("/bill/f2-report/review")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportBillRequestBody(NEXT_BILL_ID.getAndIncrement())))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillReviewResponse.class);

        assertThat(review.totalAmount()).isEqualByComparingTo("125.00");
        assertThat(countBills()).isEqualTo(countBefore);
    }

    @Test
    void fullRefreshIsANoOp() throws Exception {
        mockMvc.perform(post("/bill/f2-report/full-refresh").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/bill/f2-report")).andExpect(status().isUnauthorized());
    }

    private BillResponse createBill(int billId) throws Exception {
        String requestJson = objectMapper.writeValueAsString(reportBillRequestBody(billId));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

        try {
            return objectMapper.readValue(mockMvc.perform(multipart("/bill/f2-report")
                            .file(requestPart)
                            .with(jwt()))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsByteArray(), BillResponse.class);
        } catch (Exception e) {
            // S006 ("Pristupna točka nije ovlaštena za dostavu podataka") means the Eposlovanje
            // sandbox account behind eposlovanje-mock's baked-in API key isn't registered as an
            // access point for the eReporting-to-Porezna-uprava channel — a separate authorization
            // from ordinary UBL document sending (which works fine, see F2BillControllerIT). No
            // field in the request or test config controls this; it's an account-level grant on
            // Eposlovanje's side, outside this repo's control. Skip rather than fail so this
            // self-heals once that access point is authorized for the sandbox account.
            Assumptions.assumeTrue(!isS006AccessPointNotAuthorized(e),
                    "Eposlovanje sandbox account is not authorized as an eReporting access point (S006)");
            throw e;
        }
    }

    private static boolean isS006AccessPointNotAuthorized(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains("'S006'")) {
                return true;
            }
        }
        return false;
    }

    private long countBills() throws Exception {
        return objectMapper.readValue(mockMvc.perform(get("/bill/f2-report").with(jwt()))
                .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class).length;
    }

    private Map<String, Object> reportBillRequestBody(int billId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("billId", billId);
        request.put("profile", "P1");
        request.put("vatCategory", "Pdv25");
        request.put("billDate", LocalDate.now().toString());
        request.put("billTime", "11:00:00");
        request.put("dueDate", LocalDate.now().plusDays(15).toString());
        request.put("buyerOib", "73660371074");
        request.put("baseAmount", "100.00");
        request.put("billItemName", "Test item");
        request.put("billItemDescription", "Test item description");
        request.put("buyerName", "Test Buyer d.o.o.");
        request.put("buyerStreet", "Test Street 1");
        request.put("buyerCity", "Zagreb");
        request.put("buyerPostalZone", "10000");
        return request;
    }
}
