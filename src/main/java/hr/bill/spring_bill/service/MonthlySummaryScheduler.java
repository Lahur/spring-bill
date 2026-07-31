package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.MonthlySummaryRepository;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.model.MonthlySummaryEntity;
import hr.bill.spring_bill.service.document.BillStrategy;
import hr.bill.spring_bill.service.document.PaidUnpaidTotals;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthlySummaryScheduler {

    private final MonthlySummaryRepository monthlySummaryRepository;

    private final List<BillStrategy> strategies;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        backfillFromEarliestBills();
    }

    @Scheduled(cron = "${bill.schedule.monthly-summary-cron}")
    public void fillMissingMonths() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        LocalDate month = monthlySummaryRepository.findFirstByOrderByMonthDesc()
                .map(entity -> entity.getMonth().plusMonths(1))
                .orElseGet(() -> currentMonth.minusMonths(1));

        fillRange(month, currentMonth);
    }

    /**
     * One-time manual bootstrap: only runs when the table is completely empty. Finds the earliest
     * bill month across every strategy's own upstream source and backfills every completed month
     * from there, instead of the regular cron's single-month-back fallback.
     */
    public void backfillFromEarliestBills() {
        if (monthlySummaryRepository.count() > 0) {
            return;
        }

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate earliestMonth = strategies.stream()
                .map(BillStrategy::findEarliestBillMonth)
                .flatMap(Optional::stream)
                .min(LocalDate::compareTo)
                .orElse(currentMonth.minusMonths(1));

        fillRange(earliestMonth, currentMonth);
    }

    private void fillRange(LocalDate from, LocalDate currentMonth) {
        for (LocalDate month = from; month.isBefore(currentMonth); month = month.plusMonths(1)) {
            monthlySummaryRepository.save(buildSummary(month));
        }
    }

    private MonthlySummaryEntity buildSummary(LocalDate month) {
        PaidUnpaidTotals sales = strategies.stream()
                .filter(strategy -> strategy.getType() != BillReportType.INGOING)
                .map(strategy -> strategy.getMonthlyTotals(month))
                .reduce(PaidUnpaidTotals.zero(), PaidUnpaidTotals::add);

        PaidUnpaidTotals purchases = strategies.stream()
                .filter(strategy -> strategy.getType() == BillReportType.INGOING)
                .map(strategy -> strategy.getMonthlyTotals(month))
                .reduce(PaidUnpaidTotals.zero(), PaidUnpaidTotals::add);

        return MonthlySummaryEntity.builder()
                .month(month)
                .salesPaidTotal(sales.paid())
                .salesUnpaidTotal(sales.unpaid())
                .purchasesPaidTotal(purchases.paid())
                .purchasesUnpaidTotal(purchases.unpaid())
                .build();
    }
}