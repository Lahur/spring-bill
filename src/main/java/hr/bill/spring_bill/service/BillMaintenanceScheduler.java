package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.service.document.BillStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillMaintenanceScheduler {

    private final List<BillStrategy> strategies;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        deleteAllExceptReports();
        syncAll();
    }

    @Scheduled(cron = "${bill.schedule.sync-cron}")
    public void syncAll() {
        strategies.forEach(BillStrategy::sync);
    }

    @Scheduled(cron = "${bill.schedule.delete-cron}")
    public void deleteAllExceptReports() {
        strategies.stream()
                .filter(strategy -> strategy.getType() != BillReportType.F2_REPORT)
                .forEach(BillStrategy::deleteAll);
    }
}