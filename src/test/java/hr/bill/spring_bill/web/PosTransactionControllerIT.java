package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.PosTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link PosTransactionController} over HTTP. There's no create endpoint — POS
 * transactions only ever come from {@code BankStatementImportService} — so the precondition here
 * is seeded straight through the repository, via {@link #seedBankTransaction}. */
class PosTransactionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PosTransactionRepository posTransactionRepository;

    @Test
    void uploadingAPdfMarksTheTransactionAsHavingABill() throws Exception {
        PosTransactionEntity entity = seedPosTransaction();
        MockMultipartFile pdf = new MockMultipartFile("file", "receipt.pdf", "application/pdf", "%PDF-1.4".getBytes());

        PosTransactionResponse uploaded = objectMapper.readValue(mockMvc.perform(multipart("/pos-transaction/" + entity.getId() + "/upload")
                        .file(pdf)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), PosTransactionResponse.class);

        assertThat(uploaded.hasBill()).isTrue();

        PosTransactionResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/pos-transaction").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), PosTransactionResponse[].class);
        assertThat(Arrays.stream(listed).filter(r -> r.id().equals(entity.getId())).findFirst())
                .hasValueSatisfying(r -> assertThat(r.hasBill()).isTrue());
    }

    @Test
    void uploadingANonPdfFileIsRejected() throws Exception {
        PosTransactionEntity entity = seedPosTransaction();
        MockMultipartFile notPdf = new MockMultipartFile("file", "receipt.txt", "text/plain", "not a pdf".getBytes());

        mockMvc.perform(multipart("/pos-transaction/" + entity.getId() + "/upload").file(notPdf).with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadingToAnUnknownTransactionIsNotFound() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("file", "receipt.pdf", "application/pdf", "%PDF-1.4".getBytes());

        mockMvc.perform(multipart("/pos-transaction/" + UUID.randomUUID() + "/upload").file(pdf).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/pos-transaction")).andExpect(status().isUnauthorized());
    }

    private PosTransactionEntity seedPosTransaction() {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("18.90"), CreditDebitIndicator.DBIT);
        return posTransactionRepository.save(PosTransactionEntity.builder().bankTransaction(tx).build());
    }
}
