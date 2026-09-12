package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.RecipientRepository;
import hr.bill.spring_bill.model.RecipientEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link RecipientController} over HTTP. There's no create endpoint — recipients are
 * only ever recorded as a side effect of {@code DocumentService.generateAndSendDocuments}, so the
 * precondition here is seeded straight through the repository. */
class RecipientControllerIT extends AbstractIntegrationTest {

    @Autowired
    private RecipientRepository recipientRepository;

    @Test
    void listsRecipientsOrderedByEmail() throws Exception {
        String first = "a-" + UUID.randomUUID() + "@example.com";
        String second = "z-" + UUID.randomUUID() + "@example.com";
        recipientRepository.save(RecipientEntity.builder().email(second).build());
        recipientRepository.save(RecipientEntity.builder().email(first).build());

        String[] emails = objectMapper.readValue(mockMvc.perform(get("/recipient").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), String[].class);

        List<String> ours = List.of(emails).stream().filter(e -> e.equals(first) || e.equals(second)).toList();
        assertThat(ours).containsExactly(first, second);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/recipient")).andExpect(status().isUnauthorized());
    }
}
