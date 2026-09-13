package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Both {@link BillPdfClient} and {@link MailBillClient} are pointed at real containers from
 * {@link AbstractIntegrationTest} (the {@code hub-bill} PDF renderer and the Mailhog-backed
 * {@code mail-bill-test} image) rather than stubbed. */
class BankStatementControllerIT extends AbstractIntegrationTest {

    private static final String RECIPIENT_EMAIL = "finance@example.com";

    @Autowired
    private BankStatementRepository bankStatementRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private PosTransactionRepository posTransactionRepository;

    @Autowired
    private CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

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
    void uploadingWithAnEmailRendersAndSendsTheGeneratedPdf() throws Exception {
        long mailCountBefore = mailhogMessagesTo(RECIPIENT_EMAIL);

        mockMvc.perform(multipart("/bank-statement/upload")
                        .file(statementFilePart("bank-statement-2.xml"))
                        .param("email", RECIPIENT_EMAIL)
                        .with(jwt()))
                .andExpect(status().isOk());

        assertThat(mailhogMessagesTo(RECIPIENT_EMAIL)).isEqualTo(mailCountBefore + 1);
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
