package hr.bill.spring_bill;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.config.tenant.TenantFilter;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dao.RecipientRepository;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.RecipientEntity;
import hr.bill.spring_bill.model.enums.BillType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the X-Tenant-Id header is enforced and that one tenant never sees another tenant's rows. */
class TenantIsolationIT extends AbstractIntegrationTest {

    private static final UUID OTHER_TENANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private BillRepository billRepository;

    @Test
    void requestWithoutTenantHeaderIsRejected() throws Exception {
        mockMvc.perform(get("/recipient").with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestWithNonUuidTenantHeaderIsRejected() throws Exception {
        mockMvc.perform(get("/recipient").with(SecurityMockMvcRequestPostProcessors.jwt())
                        .header(TenantFilter.HEADER, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestWithoutTenantHeaderIsStillUnauthorized() throws Exception {
        mockMvc.perform(get("/recipient")).andExpect(status().isUnauthorized());
    }

    @Test
    void tenantOnlySeesItsOwnRowsOverHttp() throws Exception {
        String email = "isolation-" + UUID.randomUUID() + "@example.com";
        recipientRepository.save(RecipientEntity.builder().email(email).build());

        assertThat(recipients(TEST_TENANT_ID)).contains(email);
        assertThat(recipients(OTHER_TENANT_ID)).doesNotContain(email);
    }

    @Test
    void lookupByIdDoesNotCrossTenants() {
        UUID id = recipientRepository.save(RecipientEntity.builder()
                .email("by-id-" + UUID.randomUUID() + "@example.com").build()).getId();

        TenantContext.runAs(OTHER_TENANT_ID, () -> assertThat(recipientRepository.findById(id)).isEmpty());
        assertThat(recipientRepository.findById(id)).isPresent();
    }

    @Test
    void sameNaturalKeyCanExistInEachTenant() {
        String email = "shared-" + UUID.randomUUID() + "@example.com";
        recipientRepository.save(RecipientEntity.builder().email(email).build());

        TenantContext.runAs(OTHER_TENANT_ID, () -> {
            RecipientEntity saved = recipientRepository.save(RecipientEntity.builder().email(email).build());
            assertThat(saved.getTenantId()).isEqualTo(OTHER_TENANT_ID);
        });
    }

    @Test
    void nativeQueriesAreScopedToTheCurrentTenant() {
        LocalDateTime billDate = LocalDateTime.of(2001, 1, 15, 12, 0);
        billRepository.save(BillEntity.builder()
                .systemId(System.nanoTime())
                .fullBillId("ISO-" + UUID.randomUUID())
                .clientName("Isolation client")
                .billDate(billDate)
                .totalAmount(new BigDecimal("12.34"))
                .billType(BillType.values()[0])
                .build());
        String billType = BillType.values()[0].name();
        LocalDateTime from = billDate.withDayOfMonth(1);
        LocalDateTime to = from.plusMonths(1);

        assertThat(billRepository.sumUnpaidAmountByBillTypeAndBillDateBetween(billType, from, to))
                .isEqualByComparingTo("12.34");
        TenantContext.runAs(OTHER_TENANT_ID, () ->
                assertThat(billRepository.sumUnpaidAmountByBillTypeAndBillDateBetween(billType, from, to))
                        .isEqualByComparingTo("0"));
    }

    private List<String> recipients(UUID tenantId) throws Exception {
        return List.of(objectMapper.readValue(mockMvc.perform(get("/recipient").with(jwtFor(tenantId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), String[].class));
    }
}
