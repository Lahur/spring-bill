package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link AccountsStatementController} over HTTP. */
class AccountsStatementControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    @Test
    void createsListsAndUploadsABillForAnAccountsStatement() throws Exception {
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "amount", "150.00",
                "description", "ATM withdrawal " + UUID.randomUUID(),
                "date", LocalDate.now().toString()));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

        AccountsStatementResponse created = objectMapper.readValue(mockMvc.perform(multipart("/accounts-statement")
                        .file(requestPart)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), AccountsStatementResponse.class);

        assertThat(created.amount()).isEqualByComparingTo("150.00");
        assertThat(created.hasBill()).isFalse();

        AccountsStatementResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/accounts-statement").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), AccountsStatementResponse[].class);
        assertThat(Arrays.stream(listed).map(AccountsStatementResponse::id)).contains(created.id());

        MockMultipartFile pdf = new MockMultipartFile("file", "statement.pdf", "application/pdf", "%PDF-1.4".getBytes());
        AccountsStatementResponse uploaded = objectMapper.readValue(mockMvc.perform(multipart("/accounts-statement/" + created.id() + "/upload")
                        .file(pdf)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), AccountsStatementResponse.class);
        assertThat(uploaded.hasBill()).isTrue();
    }

    @Test
    void syncingLinksAndDeductsFromACashWithdrawalBalance() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("500.00"), CreditDebitIndicator.DBIT);
        CashWithdrawalBalanceEntity balance = cashWithdrawalBalanceRepository.save(CashWithdrawalBalanceEntity.builder()
                .total(new BigDecimal("500.00"))
                .balance(new BigDecimal("500.00"))
                .bankTransaction(tx)
                .build());

        String requestJson = objectMapper.writeValueAsString(Map.of(
                "amount", "150.00",
                "description", "ATM withdrawal " + UUID.randomUUID(),
                "date", LocalDate.now().toString()));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());
        AccountsStatementResponse created = objectMapper.readValue(mockMvc.perform(multipart("/accounts-statement")
                        .file(requestPart)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), AccountsStatementResponse.class);

        AccountsStatementResponse[] synced = objectMapper.readValue(mockMvc.perform(post("/accounts-statement/sync")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(created.id())))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), AccountsStatementResponse[].class);

        assertThat(synced).hasSize(1);
        assertThat(synced[0].cashWithdrawalBalanceIds()).contains(balance.getId());
        assertThat(cashWithdrawalBalanceRepository.findById(balance.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("350.00");
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/accounts-statement")).andExpect(status().isUnauthorized());
    }
}
