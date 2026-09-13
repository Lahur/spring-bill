package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerIT extends AbstractIntegrationTest {

    private static final String RECIPIENT_EMAIL = "finance@example.com";

    @Test
    void generatesAndSendsADocumentForAnExistingBankStatement() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("10.00"), CreditDebitIndicator.CRDT);
        String statementId = tx.getBankStatement().getId().toString();
        long mailCountBefore = mailhogMessagesTo(RECIPIENT_EMAIL);

        String requestBody = objectMapper.writeValueAsString(SendBillReportsRequest.builder()
                .reports(List.of(SendBillReportItem.builder()
                        .id(statementId)
                        .billId(statementId)
                        .type(BillReportType.BANK_STATEMENT)
                        .build()))
                .email(RECIPIENT_EMAIL)
                .build());

        mockMvc.perform(post("/document/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isOk());

        assertThat(mailhogMessagesTo(RECIPIENT_EMAIL)).isEqualTo(mailCountBefore + 1);
    }

    @Test
    void separatedRequestAttachesEachBillIndividually() throws Exception {
        BankTransactionEntity tx1 = seedBankTransaction(new BigDecimal("10.00"), CreditDebitIndicator.CRDT);
        BankTransactionEntity tx2 = seedBankTransaction(new BigDecimal("20.00"), CreditDebitIndicator.CRDT);
        String id1 = tx1.getBankStatement().getId().toString();
        String id2 = tx2.getBankStatement().getId().toString();
        long mailCountBefore = mailhogMessagesTo(RECIPIENT_EMAIL);

        String requestBody = objectMapper.writeValueAsString(SendBillReportsRequest.builder()
                .reports(List.of(
                        SendBillReportItem.builder().id(id1).billId(id1).type(BillReportType.BANK_STATEMENT).build(),
                        SendBillReportItem.builder().id(id2).billId(id2).type(BillReportType.BANK_STATEMENT).build()))
                .email(RECIPIENT_EMAIL)
                .separated(true)
                .build());

        mockMvc.perform(post("/document/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isOk());

        assertThat(mailhogMessagesTo(RECIPIENT_EMAIL)).isEqualTo(mailCountBefore + 1);

        // DocumentService.individualFiles names each attachment "<title>-<n>-<total>" (mail-bill adds
        // the ".pdf" extension); both bills share a rendering order (both BANK_STATEMENT), so which
        // one lands as "-1-2" vs "-2-2" isn't predictable — but the exact pair of names is, regardless
        // of order.
        MailhogMessage message = mailhogLatestMessageTo(RECIPIENT_EMAIL);
        assertThat(message.attachments())
                .extracting(MailhogAttachment::fileName)
                .containsExactlyInAnyOrder(message.subject() + "-1-2.pdf", message.subject() + "-2-2.pdf");
    }

    @Test
    void rejectsMoreThanSixIndividualReports() throws Exception {
        List<SendBillReportItem> sevenReports = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> SendBillReportItem.builder()
                        .id(String.valueOf(i))
                        .billId(String.valueOf(i))
                        .type(BillReportType.BANK_STATEMENT)
                        .build())
                .toList();
        String requestBody = objectMapper.writeValueAsString(SendBillReportsRequest.builder()
                .reports(sevenReports)
                .email(RECIPIENT_EMAIL)
                .separated(true)
                .build());

        mockMvc.perform(post("/document/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/document/generate-and-send").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
