package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link TenantPropertyController} over HTTP. */
class TenantPropertyControllerIT extends AbstractIntegrationTest {

    private static final String PROPERTY_URL = "/tenant/" + TEST_TENANT_ID + "/property";

    @Test
    void propertyCanBeSetReadListedAndDeleted() throws Exception {
        putProperty("STATEMENT_MAIL_TO", "{\"value\":\"statements@example.com\"}").andExpect(status().isNoContent());

        mockMvc.perform(get(PROPERTY_URL + "/STATEMENT_MAIL_TO").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.property").value("STATEMENT_MAIL_TO"))
                .andExpect(jsonPath("$.value").value("statements@example.com"));
        mockMvc.perform(get(PROPERTY_URL).with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.property == 'STATEMENT_MAIL_TO')].value").value("statements@example.com"));

        mockMvc.perform(delete(PROPERTY_URL + "/STATEMENT_MAIL_TO").with(adminJwt()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(PROPERTY_URL + "/STATEMENT_MAIL_TO").with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidValues() throws Exception {
        putProperty("STATEMENT_MAIL_TO", "{\"value\":\"not-an-email\"}").andExpect(status().isBadRequest());
        putProperty("DEPOSIT_COUNT", "{\"value\":\"abc\"}").andExpect(status().isBadRequest());
        putProperty("DEPOSIT_COUNT", "{\"value\":\"\"}").andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownProperty() throws Exception {
        putProperty("NOT_A_PROPERTY", "{\"value\":\"x\"}").andExpect(status().isBadRequest());
    }

    @Test
    void counterCanBeSet() throws Exception {
        putProperty("DEPOSIT_COUNT", "{\"value\":\"42\"}").andExpect(status().isNoContent());
        mockMvc.perform(get(PROPERTY_URL + "/DEPOSIT_COUNT").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("42"));
    }

    @Test
    void nonAdminIsForbidden() throws Exception {
        mockMvc.perform(get(PROPERTY_URL).with(jwt())).andExpect(status().isForbidden());
    }

    private ResultActions putProperty(String property, String body) throws Exception {
        return mockMvc.perform(put(PROPERTY_URL + "/" + property).with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
