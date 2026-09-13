package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.service.CroatianTimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link F2BillController} over HTTP. {@code createBill} requires the buyer OIB to pass a
 * real AMS check in the Eposlovanje sandbox, so {@link #BUYER_OIB} must be one that's actually
 * published there (26187994862, confirmed via a direct AMS check call). Every {@code createBill}/
 * {@code cancel} here sends a real UBL invoice to the sandbox — safe there, per the client.
 * {@code createBill} also renders and embeds a PDF copy of the invoice via {@link BillPdfClient},
 * which — same as every other IT class — is pointed at the real (DB-less) {@code hub-bill}
 * container from {@link AbstractIntegrationTest}, not stubbed. */
class F2BillControllerIT extends AbstractIntegrationTest {

    private static final String BUYER_OIB = "26187994862";

    // Eposlovanje matches a created document by "{billId}/1/1" among today's outgoing documents,
    // so every bill created in this run needs its own never-before-used billId.
    private static final AtomicInteger NEXT_BILL_ID = new AtomicInteger((int) (System.currentTimeMillis() % 1_000_000) + 1);

    @Test
    void createsListsAndFetchesABill() throws Exception {
        BillResponse created = createBill();

        assertThat(created.systemId()).isNotNull();
        assertThat(created.fullBillId()).isNotNull();

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/f2").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(created.id());

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/f2/" + created.systemId()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        assertThat(info.mainDataInfo()).isNotNull();
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
