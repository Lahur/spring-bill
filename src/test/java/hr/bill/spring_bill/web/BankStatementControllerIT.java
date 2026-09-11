package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.mail_bill.response.SendMailResponse;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BankStatementControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    private BillPdfClient billPdfClient;

    @MockitoBean
    private MailBillClient mailBillClient;

    @Autowired
    private BankStatementRepository bankStatementRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private PosTransactionRepository posTransactionRepository;

    @Autowired
    private CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

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
    void uploadingAStatementPersistsItAndReturnsIt() throws Exception {
        // bank-statement-1.xml: 6 entries, all plain domestic transfers (no POS/withdrawal codes
        // in this client's data), so nothing lands in Pos/CashWithdrawal. The Postgres container
        // (and its data) is shared across every test method in this class, so assert deltas.
        long posCountBefore = posTransactionRepository.count();
        long cashWithdrawalCountBefore = cashWithdrawalBalanceRepository.count();

        BankStatementResponse[] uploaded = objectMapper.readValue(mockMvc.perform(multipart("/bank-statement/upload")
                        .file(statementFilePart("bank-statement-1.xml"))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankStatementResponse[].class);

        assertThat(uploaded).hasSize(1);
        BankStatementResponse response = uploaded[0];
        assertThat(response.statementId()).isEqualTo("207550065");

        BankStatementResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bank-statement").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankStatementResponse[].class);
        assertThat(listed).extracting(BankStatementResponse::statementId).contains("207550065");

        List<BankTransactionEntity> transactions =
                bankTransactionRepository.findAllByBankStatement_IdOrderByTransactionDateAsc(response.id());
        assertThat(transactions).hasSize(6);
        assertThat(transactions).extracting(BankTransactionEntity::getTransactionType)
                .containsOnly(BankTransactionType.TRANSACTION);

        // No local bills and no matching upstream document/receipt exist for these fixture
        // references, so none of the six should have resolved a billSystemId.
        assertThat(transactions).extracting(BankTransactionEntity::getBillSystemId).containsOnlyNulls();

        assertThat(posTransactionRepository.count()).isEqualTo(posCountBefore);
        assertThat(cashWithdrawalBalanceRepository.count()).isEqualTo(cashWithdrawalCountBefore);
    }

    @Test
    void reuploadingTheSameStatementIsSkipped() throws Exception {
        mockMvc.perform(multipart("/bank-statement/upload")
                        .file(statementFilePart("bank-statement-3.xml"))
                        .with(jwt()))
                .andExpect(status().isOk());
        long countAfterFirstImport = bankTransactionRepository.count();

        BankStatementResponse[] second = objectMapper.readValue(mockMvc.perform(multipart("/bank-statement/upload")
                        .file(statementFilePart("bank-statement-3.xml"))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankStatementResponse[].class);

        assertThat(second).isEmpty();
        assertThat(bankTransactionRepository.count()).isEqualTo(countAfterFirstImport);
    }

    @Test
    void uploadingWithAnEmailRendersAndSendsThePdfThroughTheMockedClients() throws Exception {
        when(billPdfClient.renderBankStatement(any())).thenReturn(pdfBytes);
        when(mailBillClient.sendMail(any())).thenReturn(new SendMailResponse("mail-id-207656679"));

        mockMvc.perform(multipart("/bank-statement/upload")
                        .file(statementFilePart("bank-statement-2.xml"))
                        .param("email", "finance@example.com")
                        .with(jwt()))
                .andExpect(status().isOk());

        verify(billPdfClient, times(1)).renderBankStatement(any());
        verify(mailBillClient, times(1)).sendMail(any());
    }

    @Test
    void unauthenticatedUploadIsRejected() throws Exception {
        mockMvc.perform(multipart("/bank-statement/upload").file(statementFilePart("bank-statement-1.xml")))
                .andExpect(status().isUnauthorized());
    }

    private MockMultipartFile statementFilePart(String fixtureFilename) {
        try {
            byte[] xml = new ClassPathResource("camt/" + fixtureFilename).getContentAsByteArray();
            return new MockMultipartFile("files", fixtureFilename, "text/xml", xml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
