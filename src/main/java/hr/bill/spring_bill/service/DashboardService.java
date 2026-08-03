package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dao.MonthlySummaryRepository;
import hr.bill.spring_bill.dao.projection.DailyTotalProjection;
import hr.bill.spring_bill.dto.web.dashboard.DailyTotalResponse;
import hr.bill.spring_bill.dto.web.dashboard.DashboardSummaryResponse;
import hr.bill.spring_bill.dto.web.dashboard.MonthlySummaryResponse;
import hr.bill.spring_bill.model.MonthlySummaryEntity;
import hr.bill.spring_bill.model.enums.BillType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepository;

    private final MonthlySummaryRepository monthlySummaryRepository;

    public DashboardSummaryResponse getSummary() {
        log.debug("Building dashboard summary for current month");
        LocalDateTime from = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1);

        BigDecimal salesPaidTotal = billRepository.sumPaidAmountByBillTypeNotAndBillDateBetween(
                BillType.INGOING_BILL.name(), from, to);
        BigDecimal salesUnpaidTotal = billRepository.sumUnpaidAmountByBillTypeNotAndBillDateBetween(
                BillType.INGOING_BILL.name(), from, to);
        BigDecimal purchasesPaidTotal = billRepository.sumPaidAmountByBillTypeAndBillDateBetween(
                BillType.INGOING_BILL.name(), from, to);
        BigDecimal purchasesUnpaidTotal = billRepository.sumUnpaidAmountByBillTypeAndBillDateBetween(
                BillType.INGOING_BILL.name(), from, to);

        return DashboardSummaryResponse.builder()
                .salesPaidTotal(salesPaidTotal)
                .salesUnpaidTotal(salesUnpaidTotal)
                .purchasesPaidTotal(purchasesPaidTotal)
                .purchasesUnpaidTotal(purchasesUnpaidTotal)
                .build();
    }

    public List<DailyTotalResponse> getDailySales(LocalDate from, LocalDate to) {
        log.debug("Building daily sales totals from {} to {}", from, to);
        Map<LocalDate, BigDecimal> totalsByDay = billRepository
                .sumDailyTotalsByBillTypeNot(BillType.INGOING_BILL.name(), from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .collect(Collectors.toMap(DailyTotalProjection::getDay, DailyTotalProjection::getTotal));

        List<DailyTotalResponse> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            result.add(DailyTotalResponse.builder()
                    .date(date)
                    .total(totalsByDay.getOrDefault(date, BigDecimal.ZERO))
                    .build());
        }
        return result;
    }

    public List<MonthlySummaryResponse> getMonthlySummaries(int months) {
        log.debug("Fetching last {} monthly summaries", months);
        List<MonthlySummaryEntity> entities = monthlySummaryRepository.findAllByOrderByMonthDesc(PageRequest.of(0, months));
        return entities.stream()
                .sorted(Comparator.comparing(MonthlySummaryEntity::getMonth))
                .map(entity -> MonthlySummaryResponse.builder()
                        .month(entity.getMonth())
                        .salesPaidTotal(entity.getSalesPaidTotal())
                        .salesUnpaidTotal(entity.getSalesUnpaidTotal())
                        .purchasesPaidTotal(entity.getPurchasesPaidTotal())
                        .purchasesUnpaidTotal(entity.getPurchasesUnpaidTotal())
                        .build())
                .toList();
    }
}