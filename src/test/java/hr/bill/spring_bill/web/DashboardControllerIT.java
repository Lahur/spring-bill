package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.dao.MonthlySummaryRepository;
import hr.bill.spring_bill.dto.web.dashboard.DailyTotalResponse;
import hr.bill.spring_bill.dto.web.dashboard.DashboardSummaryResponse;
import hr.bill.spring_bill.dto.web.dashboard.MonthlySummaryResponse;
import hr.bill.spring_bill.model.MonthlySummaryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;

    @Test
    void summaryTotalsAreNeverNegative() throws Exception {
        DashboardSummaryResponse response = objectMapper.readValue(mockMvc.perform(get("/dashboard/summary").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), DashboardSummaryResponse.class);

        assertThat(response.salesUnpaidTotal()).isNotNegative();
        assertThat(response.purchasesUnpaidTotal()).isNotNegative();
    }

    @Test
    void dailySalesCoversEveryDayInTheRangeWithZeroTotals() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 5);

        DailyTotalResponse[] totals = objectMapper.readValue(mockMvc.perform(get("/dashboard/daily-sales")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), DailyTotalResponse[].class);

        assertThat(totals).hasSize(5);
        assertThat(totals[0].date()).isEqualTo(from);
        assertThat(totals[4].date()).isEqualTo(to);
    }

    @Test
    void monthlySummariesReturnsTheMostRecentPersistedMonths() throws Exception {
        LocalDate month = LocalDate.of(9999, 1, 1);
        monthlySummaryRepository.save(MonthlySummaryEntity.builder()
                .month(month)
                .salesPaidTotal(new BigDecimal("100.00"))
                .salesUnpaidTotal(new BigDecimal("20.00"))
                .purchasesPaidTotal(new BigDecimal("30.00"))
                .purchasesUnpaidTotal(new BigDecimal("5.00"))
                .build());

        MonthlySummaryResponse[] summaries = objectMapper.readValue(mockMvc.perform(get("/dashboard/monthly-summary")
                        .param("months", "1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), MonthlySummaryResponse[].class);

        assertThat(summaries).hasSize(1);
        assertThat(summaries[0].month()).isEqualTo(month);
        assertThat(summaries[0].salesPaidTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/dashboard/summary")).andExpect(status().isUnauthorized());
    }
}
