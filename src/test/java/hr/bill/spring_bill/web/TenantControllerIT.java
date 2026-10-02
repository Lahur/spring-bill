package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.TenantApiKeyRepository;
import hr.bill.spring_bill.dao.TenantRepository;
import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.model.TenantEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link TenantController} over HTTP. {@link AbstractIntegrationTest} re-seeds
 * {@code TEST_TENANT} before every test, so changes made here don't leak into other ITs. */
class TenantControllerIT extends AbstractIntegrationTest {

    private static final String TENANT_URL = "/tenant/" + TEST_TENANT_ID;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TenantApiKeyRepository tenantApiKeyRepository;

    @Test
    void returnsSeededTenant() throws Exception {
        TenantDto tenant = objectMapper.readValue(mockMvc.perform(get(TENANT_URL).with(adminJwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), TenantDto.class);

        assertThat(tenant).isEqualTo(TEST_TENANT);
    }

    @Test
    void putReplacesTheCurrentTenantsRow() throws Exception {
        TenantDto updated = TenantDto.builder()
                .oib(TEST_TENANT.oib())
                .name("Renamed d.o.o.")
                .street("New Street 2")
                .city("Split")
                .postalZone("21000")
                .countryCode("HR")
                .contactOib(TEST_TENANT.contactOib())
                .contactName("New Contact")
                .phone(null)
                .email(null)
                .iban(TEST_TENANT.iban())
                .build();

        TenantDto response = objectMapper.readValue(mockMvc.perform(put(TENANT_URL).with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), TenantDto.class);

        assertThat(response).isEqualTo(updated);
        assertThat(tenantRepository.findById(TEST_TENANT_ID)).get()
                .extracting(TenantEntity::getName).isEqualTo("Renamed d.o.o.");
    }

    @Test
    void putCreatesANewTenantWithoutTenantHeader() throws Exception {
        UUID newTenantId = UUID.randomUUID();
        TenantDto created = TenantDto.builder()
                .oib(TEST_TENANT.oib())
                .name("New Tenant d.o.o.")
                .street(TEST_TENANT.street())
                .city(TEST_TENANT.city())
                .postalZone(TEST_TENANT.postalZone())
                .countryCode(TEST_TENANT.countryCode())
                .contactOib(TEST_TENANT.contactOib())
                .contactName(TEST_TENANT.contactName())
                .iban(TEST_TENANT.iban())
                .build();

        mockMvc.perform(put("/tenant/" + newTenantId).with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk());

        assertThat(tenantRepository.findById(newTenantId)).get()
                .extracting(TenantEntity::getName).isEqualTo("New Tenant d.o.o.");
        tenantRepository.deleteById(newTenantId);
    }

    @Test
    void putRejectsBlankRequiredFields() throws Exception {
        TenantDto invalid = TenantDto.builder().oib("").build();

        mockMvc.perform(put(TENANT_URL).with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminIsForbidden() throws Exception {
        mockMvc.perform(get(TENANT_URL).with(jwt())).andExpect(status().isForbidden());
        mockMvc.perform(put(TENANT_URL + "/api-key").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get(TENANT_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void putApiKeysReplacesTheTenantsKeys() throws Exception {
        mockMvc.perform(put(TENANT_URL + "/api-key").with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eposlovanjeApiKey\":\"rotated-key\",\"hubTenantId\":\"tehnomodus\"}"))
                .andExpect(status().isNoContent());

        assertThat(tenantApiKeyRepository.findFirstBy()).get().satisfies(keys -> {
            assertThat(keys.getEposlovanjeApiKey()).isEqualTo("rotated-key");
            assertThat(keys.getF1WebApiKey()).isNull();
            assertThat(keys.getHubTenantId()).isEqualTo("tehnomodus");
        });
        assertThat(tenantApiKeyRepository.count()).isEqualTo(1);
    }

    @Test
    void statementMailToCanBeSetReadAndCleared() throws Exception {
        putStatementMailTo("{\"mailTo\":\"statements@example.com\"}").andExpect(status().isNoContent());
        mockMvc.perform(get(TENANT_URL + "/statement-mail-to").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailTo").value("statements@example.com"));

        putStatementMailTo("{\"mailTo\":null}").andExpect(status().isNoContent());
        mockMvc.perform(get(TENANT_URL + "/statement-mail-to").with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailTo").doesNotExist());
    }

    @Test
    void statementMailToRejectsInvalidEmail() throws Exception {
        putStatementMailTo("{\"mailTo\":\"not-an-email\"}").andExpect(status().isBadRequest());
    }

    private ResultActions putStatementMailTo(String body) throws Exception {
        return mockMvc.perform(put(TENANT_URL + "/statement-mail-to").with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
