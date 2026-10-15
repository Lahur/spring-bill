package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dto.web.banktransaction.BankTransactionResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BankTransactionType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link BankTransactionController} over HTTP. There's no create endpoint — bank
 * transactions only ever come from bank statement imports — so they're seeded straight through
 * the repository, via {@link #seedBankTransaction}. */
class BankTransactionControllerIT extends AbstractIntegrationTest {

    @Test
    void listsAllTransactionsNewestFirst() throws Exception {
        BankTransactionEntity older = seedBankTransactionAt(new BigDecimal("10.00"), LocalDateTime.now().minusDays(2));
        BankTransactionEntity newer = seedBankTransactionAt(new BigDecimal("20.00"), LocalDateTime.now().minusDays(1));

        BankTransactionResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bank-transaction").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankTransactionResponse[].class);

        assertThat(Arrays.stream(listed).map(BankTransactionResponse::id).filter(id ->
                id.equals(older.getId()) || id.equals(newer.getId())))
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void listsOnlyTheTransactionsOfTheGivenBankStatement() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("15.50"), CreditDebitIndicator.CRDT);
        seedBankTransaction(new BigDecimal("99.00"), CreditDebitIndicator.DBIT);
        UUID statementId = tx.getBankStatement().getId();

        BankTransactionResponse[] listed = objectMapper.readValue(mockMvc.perform(
                        get("/bank-transaction/bank-statement/" + statementId).with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankTransactionResponse[].class);

        assertThat(listed).singleElement().satisfies(r -> {
            assertThat(r.id()).isEqualTo(tx.getId());
            assertThat(r.bankStatementId()).isEqualTo(statementId);
            assertThat(r.amount()).isEqualByComparingTo("15.50");
            assertThat(r.creditDebitIndicator()).isEqualTo(CreditDebitIndicator.CRDT);
            assertThat(r.transactionType()).isEqualTo(BankTransactionType.TRANSACTION);
        });
    }

    @Test
    void unknownBankStatementHasNoTransactions() throws Exception {
        BankTransactionResponse[] listed = objectMapper.readValue(mockMvc.perform(
                        get("/bank-transaction/bank-statement/" + UUID.randomUUID()).with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankTransactionResponse[].class);

        assertThat(listed).isEmpty();
    }

    @Test
    void fetchesATransactionById() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("42.00"), CreditDebitIndicator.DBIT);

        BankTransactionResponse fetched = objectMapper.readValue(mockMvc.perform(
                        get("/bank-transaction/" + tx.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), BankTransactionResponse.class);

        assertThat(fetched.id()).isEqualTo(tx.getId());
        assertThat(fetched.bankStatementId()).isEqualTo(tx.getBankStatement().getId());
        assertThat(fetched.amount()).isEqualByComparingTo("42.00");
        assertThat(fetched.creditDebitIndicator()).isEqualTo(CreditDebitIndicator.DBIT);
    }

    @Test
    void fetchingAnUnknownTransactionIsNotFound() throws Exception {
        mockMvc.perform(get("/bank-transaction/" + UUID.randomUUID()).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherTenantCannotSeeTheTransaction() throws Exception {
        BankTransactionEntity tx = seedBankTransaction(new BigDecimal("7.00"), CreditDebitIndicator.CRDT);

        mockMvc.perform(get("/bank-transaction/" + tx.getId()).with(jwtFor(UUID.fromString("22222222-2222-2222-2222-222222222222"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/bank-transaction")).andExpect(status().isUnauthorized());
    }

    private BankTransactionEntity seedBankTransactionAt(BigDecimal amount, LocalDateTime transactionDate) {
        BankTransactionEntity tx = seedBankTransaction(amount, CreditDebitIndicator.CRDT);
        tx.setTransactionDate(transactionDate);
        return bankTransactionRepository.save(tx);
    }
}
