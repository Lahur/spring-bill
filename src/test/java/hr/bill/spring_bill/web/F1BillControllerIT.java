package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link F1BillController} over HTTP. {@code F1WebClient} is a real client, but
 * {@code eposlovanje-mock}'s {@code /f1-web} route is a genuine in-memory fake (not proxied
 * anywhere) — so every call here, including {@code createBill}/{@code cancel} which fiscalize a
 * receipt / storno it, is fully self-contained and safe to run repeatedly. */
class F1BillControllerIT extends AbstractIntegrationTest {

    @Test
    void createsListsAndFetchesABill() throws Exception {
        BillResponse created = createBill();

        assertThat(created.systemId()).isNotNull();
        assertThat(created.fullBillId()).isNotNull();

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/f1").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);
        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(created.id());

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/f1/" + created.systemId()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        assertThat(info.mainDataInfo()).isNotNull();
        assertThat(info.buyerInfo()).isNotNull();
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
