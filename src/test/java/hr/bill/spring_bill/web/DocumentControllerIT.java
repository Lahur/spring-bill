package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dto.mail_bill.response.SendMailResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link DocumentController} over HTTP. Only {@link BillPdfClient}/{@link MailBillClient}
 * are stubbed; the referenced bank statement is a real, persisted row. */
class DocumentControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    private BillPdfClient billPdfClient;

    @MockitoBean
    private MailBillClient mailBillClient;

    private static byte[] pdfBytes;

    @BeforeAll
    static void renderPdf() {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage(PDRectangle.A4));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            pdfBytes = out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void generatesAndSendsADocumentForAnExistingBankStatement() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("10.00"), CreditDebitIndicator.CRDT);
        String statementId = tx.getBankStatement().getId().toString();
        when(billPdfClient.renderBankStatement(any())).thenReturn(pdfBytes);
        when(mailBillClient.sendMail(any())).thenReturn(new SendMailResponse("mail-id"));

        String requestBody = objectMapper.writeValueAsString(SendBillReportsRequest.builder()
                .reports(List.of(SendBillReportItem.builder()
                        .id(statementId)
                        .billId(statementId)
                        .type(BillReportType.BANK_STATEMENT)
                        .build()))
                .email("finance@example.com")
                .build());

        mockMvc.perform(post("/document/generate-and-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(billPdfClient, times(1)).renderBankStatement(any());
        verify(mailBillClient, times(1)).sendMail(any());
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
                .email("finance@example.com")
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
