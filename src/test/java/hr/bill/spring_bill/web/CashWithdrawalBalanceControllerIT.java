package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CashWithdrawalBalanceResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link CashWithdrawalBalanceController} over HTTP. There's no create endpoint — cash
 * withdrawal balances only ever come from {@code BankStatementImportService} — so the precondition
 * here is seeded straight through the repository. */
class CashWithdrawalBalanceControllerIT extends AbstractIntegrationTest {

    @Autowired
    private CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    @Test
    void creatingAnAccountsStatementDeductsFromTheBalance() throws Exception {
        CashWithdrawalBalanceEntity balance = seedCashWithdrawalBalance(new BigDecimal("500.00"));
        String requestJson = objectMapper.writeValueAsString(Map.of(
                "cashWithdrawalIds", Map.of("1", balance.getId()),
                "amount", "150.00",
                "description", "ATM withdrawal",
                "date", LocalDate.now().toString()));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", requestJson.getBytes());

        CashWithdrawalBalanceResponse[] response = objectMapper.readValue(mockMvc.perform(multipart("/cash-withdrawal-balance/accounts-statement")
                        .file(requestPart)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), CashWithdrawalBalanceResponse[].class);

        assertThat(response).hasSize(1);
        assertThat(response[0].id()).isEqualTo(balance.getId());
        assertThat(response[0].balance()).isEqualByComparingTo("350.00");

        CashWithdrawalBalanceResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/cash-withdrawal-balance").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), CashWithdrawalBalanceResponse[].class);
        assertThat(Arrays.stream(listed).filter(r -> r.id().equals(balance.getId())).findFirst())
                .hasValueSatisfying(r -> assertThat(r.balance()).isEqualByComparingTo("350.00"));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/cash-withdrawal-balance")).andExpect(status().isUnauthorized());
    }

    private CashWithdrawalBalanceEntity seedCashWithdrawalBalance(BigDecimal amount) {
        BankTransactionEntity tx = seedBankTransaction(amount, CreditDebitIndicator.DBIT);
        return cashWithdrawalBalanceRepository.save(CashWithdrawalBalanceEntity.builder()
                .total(amount)
                .balance(amount)
                .bankTransaction(tx)
                .build());
    }
}
