package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import hr.bill.spring_bill.dto.web.bill.info.BillDocumentKind;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillItemInfo;
import hr.bill.spring_bill.dto.web.bill.info.BillPaymentMethod;
import hr.bill.spring_bill.dto.web.bill.info.BillVatRate;
import hr.bill.spring_bill.dto.web.bill.info.BuyerInfo;
import hr.bill.spring_bill.dto.web.bill.info.FiscalInfo;
import hr.bill.spring_bill.dto.web.bill.info.ItemUnitOfMeasure;
import hr.bill.spring_bill.dto.web.bill.info.MainDataInfo;
import hr.bill.spring_bill.dto.web.bill.info.PriceInfo;
import hr.bill.spring_bill.dto.web.bill.info.SupplierInfo;
import hr.bill.spring_bill.model.enums.BillType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class F1BillControllerIT extends AbstractIntegrationTest {

    @Autowired
    private SupplierProperties supplierProperties;

    @Test
    void createsListsAndFetchesABill() throws Exception {
        Map<String, Object> requestBody = f1BillRequestBody();
        BillResponse created = createBill();

        BillResponse expected = BillResponse.builder()
                .clientName((String) requestBody.get("buyerName"))
                .clientOib((String) requestBody.get("buyerOib"))
                .billDate(LocalDate.now().atTime(11, 0))
                .totalAmount(new BigDecimal("125.00"))
                .documentStatus(null)
                .billType(BillType.F1_BILL)
                .sentCount(0)
                .build();
        assertThat(created)
                .usingRecursiveComparison()
                .ignoringFields("id", "systemId", "fullBillId")
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expected);
        assertThat(created.id()).isNotNull();
        assertThat(created.systemId()).isNotNull();
        assertThat(created.fullBillId()).isNotNull();

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/f1").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(created.id());

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/f1/" + created.systemId()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        // BillInfoMapper's ReceiptDto-based mappings also reflect f1-web's response verbatim: buyer
        // info, item info and price info round-trip the request unchanged, documentKind/paymentMethod/
        // fiscalStatus are deterministic from the fixed Standard/BankTransfer/autoFiscalize(true)
        // request, and f1-web's fake always reports businessName "Mock Business d.o.o." with a null
        // businessOib for businessId 17234 — only the fiscalization-generated zki/jir/fiscalizedAt are
        // genuinely outside our control.
        MainDataInfo expectedMainDataInfo = new MainDataInfo(
                null,
                LocalDate.now().atTime(11, 0),
                LocalDate.now().plusDays(15),
                BillDocumentKind.CommercialInvoice,
                BillPaymentMethod.CreditTransfer,
                FiscalStatus.Success,
                "EUR",
                null,
                null);
        BuyerInfo expectedBuyerInfo = new BuyerInfo(
                (String) requestBody.get("buyerName"),
                (String) requestBody.get("buyerOib"),
                (String) requestBody.get("buyerAddress"),
                (String) requestBody.get("buyerCity"),
                (String) requestBody.get("buyerPostalCode"));
        SupplierInfo expectedSupplierInfo = new SupplierInfo(
                "Mock Business d.o.o.", null,
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
                new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("125.00"),
                new BigDecimal("25.00"), BillVatRate.Pdv25, "EUR");
        PriceInfo expectedPriceInfo = new PriceInfo(
                new BigDecimal("100.00"), new BigDecimal("25.00"), new BigDecimal("125.00"),
                null, new BigDecimal("125.00"));
        BillInfoResponse expectedInfo = new BillInfoResponse(expectedMainDataInfo, expectedBuyerInfo,
                expectedSupplierInfo, List.of(expectedItemInfo), null,
                new FiscalInfo(null, null, null), expectedPriceInfo);
        assertThat(info)
                .usingRecursiveComparison()
                .ignoringFields("fiscalInfo.zki", "fiscalInfo.jir", "fiscalInfo.fiscalizedAt")
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedInfo);
        assertThat(info.fiscalInfo().zki()).isNotBlank();
        assertThat(info.fiscalInfo().jir()).isNotBlank();
        assertThat(info.fiscalInfo().fiscalizedAt()).isNotNull();
    }

    @Test
    void filtersBillsByDateRange() throws Exception {
        BillResponse created = createBill();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("dateFrom", LocalDate.now().minusDays(1).toString());

        BillResponse[] filtered = objectMapper.readValue(mockMvc.perform(post("/bill/f1/filter")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(filtered).map(BillResponse::systemId)).contains(created.systemId());
    }

    @Test
    void cancelsAFiscalizedBill() throws Exception {
        BillResponse created = createBill();

        BillResponse cancelled = objectMapper.readValue(mockMvc.perform(post("/bill/f1/cancel")
                        .param("originalId", String.valueOf(created.systemId()))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse.class);

        assertThat(cancelled.systemId()).isNotEqualTo(created.systemId());
    }

    @Test
    void reviewsABillWithoutPersistingIt() throws Exception {
        long countBefore = countBills();

        BillReviewResponse review = objectMapper.readValue(mockMvc.perform(post("/bill/f1/review")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f1BillRequestBody()))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillReviewResponse.class);

        assertThat(review.totalAmount()).isEqualByComparingTo("125.00");
        assertThat(countBills()).isEqualTo(countBefore);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/bill/f1")).andExpect(status().isUnauthorized());
    }

    private BillResponse createBill() throws Exception {
        return objectMapper.readValue(mockMvc.perform(post("/bill/f1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f1BillRequestBody()))
                        .with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse.class);
    }

    private long countBills() throws Exception {
        return objectMapper.readValue(mockMvc.perform(get("/bill/f1").with(jwt()))
                .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class).length;
    }

    private Map<String, Object> f1BillRequestBody() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("billDate", LocalDate.now().toString());
        request.put("billTime", "11:00:00");
        request.put("dueDate", LocalDate.now().plusDays(15).toString());
        request.put("buyerOib", "73660371074");
        request.put("baseAmount", "100.00");
        request.put("billItemName", "Test item");
        request.put("billItemDescription", "Test item description");
        request.put("buyerName", "Test Buyer");
        request.put("buyerAddress", "Test Street 1");
        request.put("buyerCity", "Zagreb");
        request.put("buyerPostalCode", "10000");
        request.put("taxRate", 25.0);
        return request;
    }
}
