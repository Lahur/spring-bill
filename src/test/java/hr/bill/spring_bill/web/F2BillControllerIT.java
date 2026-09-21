package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.web.BusinessCheckResponse;
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
import hr.bill.spring_bill.service.CroatianTimeZone;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class F2BillControllerIT extends AbstractIntegrationTest {

    private static final String BUYER_OIB = "26187994862";

    private static final AtomicInteger NEXT_BILL_ID = new AtomicInteger((int) (System.currentTimeMillis() % 1_000_000) + 1);

    @Autowired
    private SupplierProperties supplierProperties;

    @Autowired
    private BillRepository billRepository;

    @Test
    void createsListsAndFetchesABill() throws Exception {
        int billId = NEXT_BILL_ID.get();
        BusinessCheckResponse buyer = objectMapper.readValue(mockMvc.perform(get("/business-entity/check")
                        .param("oib", BUYER_OIB)
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BusinessCheckResponse.class);
        BillResponse created = createBill();

        BillResponse expected = BillResponse.builder()
                .fullBillId(billId + "/1/1")
                .clientName(buyer.businessEntity().name())
                .clientOib(BUYER_OIB)
                .totalAmount(new BigDecimal("125.00"))
                .billType(BillType.F2_BILL)
                .sentCount(0)
                .build();
        assertThat(created)
                .usingRecursiveComparison()
                .ignoringFields("id", "systemId", "billDate", "documentStatus")
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expected);
        assertThat(created.id()).isNotNull();
        assertThat(created.systemId()).isNotNull();
        assertThat(created.billDate().toLocalDate()).isEqualTo(LocalDate.now(CroatianTimeZone.ZONE));
        assertThat(created.documentStatus()).isNotNull();

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/f2").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(created.id());

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/f2/" + created.systemId()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        // getBillInfo parses the UBL invoice back from the sandbox, which is what we generated and
        // sent — every field of it (buyer/supplier from businessEntity/supplierProperties, the single
        // item, payment means, price totals) is therefore derivable from the request/config, so the
        // whole response is compared in one go rather than spot-checking individual fields.
        MainDataInfo expectedMainDataInfo = new MainDataInfo(
                LocalDate.now(CroatianTimeZone.ZONE),
                null,
                LocalDate.now(CroatianTimeZone.ZONE).plusDays(15),
                BillDocumentKind.CommercialInvoice,
                null,
                null,
                "EUR",
                LocalDate.now(CroatianTimeZone.ZONE).withDayOfMonth(1),
                LocalDate.now(CroatianTimeZone.ZONE));
        BuyerInfo expectedBuyerInfo = new BuyerInfo(
                buyer.businessEntity().name(),
                "HR" + BUYER_OIB,
                buyer.businessEntity().headquatersAddress(),
                buyer.businessEntity().headquatersCity(),
                buyer.businessEntity().headquatersZip());
        SupplierInfo expectedSupplierInfo = new SupplierInfo(
                supplierProperties.name(),
                "HR" + supplierProperties.oib(),
                supplierProperties.street(),
                supplierProperties.city(),
                supplierProperties.postalZone(),
                supplierProperties.contactName(),
                supplierProperties.contactOib(),
                supplierProperties.email(),
                supplierProperties.phone());
        BillItemInfo expectedItemInfo = new BillItemInfo(
                "Test item", "Test item description",
                BigDecimal.ONE, ItemUnitOfMeasure.H87,
                null, new BigDecimal("100.00"), new BigDecimal("125.00"), null,
                BillVatRate.Pdv25, null);
        PaymentInfo expectedPaymentInfo = new PaymentInfo(
                BillPaymentMethod.CreditTransfer,
                LocalDate.now(CroatianTimeZone.ZONE).plusDays(15),
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
        BillResponse created = createBill();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("dateFrom", LocalDate.now(CroatianTimeZone.ZONE).minusDays(1).toString());

        BillResponse[] filtered = objectMapper.readValue(mockMvc.perform(post("/bill/f2/filter")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(filtered).map(BillResponse::systemId)).contains(created.systemId());
    }

    @Test
    void cancelsABill() throws Exception {
        BillResponse created = createBill();

        BillResponse cancelled = objectMapper.readValue(mockMvc.perform(post("/bill/f2/cancel")
                        .param("originalId", String.valueOf(created.systemId()))
                        .param("newId", String.valueOf(NEXT_BILL_ID.getAndIncrement()))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse.class);

        assertThat(cancelled.systemId()).isNotEqualTo(created.systemId());
    }

    @Test
    void fullRefreshRestoresCurrentMonthBills() throws Exception {
        BillResponse created = createBill();
        billRepository.deleteById(created.id());
        assertThat(billRepository.findBySystemIdAndBillType(created.systemId(), BillType.F2_BILL)).isEmpty();

        int refreshed = Integer.parseInt(mockMvc.perform(post("/bill/f2/full-refresh").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(refreshed).isPositive();
        assertThat(billRepository.findBySystemIdAndBillType(created.systemId(), BillType.F2_BILL)).isPresent();
    }

    @Test
    void fullRefreshMarksBillsPaidFromImportedBankStatements() throws Exception {
        BillResponse created = createBill();
        billRepository.deleteById(created.id());
        BankTransactionEntity payment = seedBankTransaction(new BigDecimal("125.00"), CreditDebitIndicator.CRDT);
        payment.setReceiverIban(supplierProperties.iban());
        payment.setReference(HrPaymentReferenceService.buildReference(created.fullBillId()));
        payment.setTransactionDate(LocalDateTime.now(CroatianTimeZone.ZONE));
        bankTransactionRepository.save(payment);

        mockMvc.perform(post("/bill/f2/full-refresh").with(jwt())).andExpect(status().isOk());

        BillEntity refreshed = billRepository.findBySystemIdAndBillType(created.systemId(), BillType.F2_BILL).orElseThrow();
        assertThat(refreshed.getDocumentStatus()).isEqualTo(BillDocumentStatus.PlacenUPotpunosti);
        assertThat(bankTransactionRepository.findById(payment.getId()).orElseThrow().getBillSystemId())
                .isEqualTo(String.valueOf(created.systemId()));
    }

    @Test
    void reviewsABillWithoutPersistingIt() throws Exception {
        long countBefore = countBills();

        BillReviewResponse review = objectMapper.readValue(mockMvc.perform(post("/bill/f2/review")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f2BillRequestBody(NEXT_BILL_ID.getAndIncrement())))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillReviewResponse.class);

        assertThat(review.totalAmount()).isEqualByComparingTo("125.00");
        assertThat(countBills()).isEqualTo(countBefore);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/bill/f2")).andExpect(status().isUnauthorized());
    }

    private BillResponse createBill() throws Exception {
        String requestJson = objectMapper.writeValueAsString(f2BillRequestBody(NEXT_BILL_ID.getAndIncrement()));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

        return objectMapper.readValue(mockMvc.perform(multipart("/bill/f2")
                        .file(requestPart)
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse.class);
    }

    private long countBills() throws Exception {
        return objectMapper.readValue(mockMvc.perform(get("/bill/f2").with(jwt()))
                .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class).length;
    }

    private Map<String, Object> f2BillRequestBody(int billId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("billId", billId);
        request.put("profile", "P1");
        request.put("vatCategory", "Pdv25");
        request.put("billDate", LocalDate.now(CroatianTimeZone.ZONE).toString());
        request.put("billTime", "11:00:00");
        request.put("dueDate", LocalDate.now(CroatianTimeZone.ZONE).plusDays(15).toString());
        request.put("buyerOib", BUYER_OIB);
        request.put("baseAmount", "100.00");
        request.put("billItemName", "Test item");
        request.put("billItemDescription", "Test item description");
        return request;
    }
}
