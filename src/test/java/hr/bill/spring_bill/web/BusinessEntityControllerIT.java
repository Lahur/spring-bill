package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dto.web.BusinessCheckResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessEntityControllerIT extends AbstractIntegrationTest {

    @Test
    void checksByOib() throws Exception {
        BusinessCheckResponse response = objectMapper.readValue(mockMvc.perform(get("/business-entity/check")
                        .param("oib", "99999999999")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BusinessCheckResponse.class);

        assertThat(response.amsCheckResponse()).isNotNull();
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/business-entity/check").param("oib", "99999999999"))
                .andExpect(status().isUnauthorized());
    }
}
