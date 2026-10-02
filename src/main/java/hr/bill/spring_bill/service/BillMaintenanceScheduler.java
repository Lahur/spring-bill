package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.service.document.BillStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillMaintenanceScheduler {

    private final List<BillStrategy> strategies;

    private final TenantService tenantService;

    @Value("${bill.schedule.delete-on-startup}")
    private boolean deleteOnStartup;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Application ready, running startup bill maintenance");
        tenantService.forEachTenant("startup bill maintenance", () -> {
            if (deleteOnStartup) {
                deleteAllExceptReportsForTenant();
            } else {
                log.info("Skipping startup deletion, bill.schedule.delete-on-startup is false");
            }
            syncAllForTenant();
        });
    }

    @Scheduled(cron = "${bill.schedule.sync-cron}")
    public void syncAll() {
        tenantService.forEachTenant("bill sync", this::syncAllForTenant);
    }

    @Scheduled(cron = "${bill.schedule.delete-cron}")
    public void deleteAllExceptReports() {
        tenantService.forEachTenant("bill deletion", this::deleteAllExceptReportsForTenant);
    }

    private void syncAllForTenant() {
        log.info("Syncing {} bill strategy/strategies", strategies.size());
        strategies.forEach(strategy -> {
            log.debug("Syncing strategy {}", strategy.getType());
            strategy.sync();
        });
        log.info("Finished syncing bill strategies");
    }

    private void deleteAllExceptReportsForTenant() {
        log.info("Deleting all bills except {} strategy", BillReportType.F2_REPORT);
        strategies.stream()
                .filter(strategy -> strategy.getType() != BillReportType.F2_REPORT)
                .forEach(strategy -> {
                    log.debug("Deleting all bills for strategy {}", strategy.getType());
                    strategy.deleteAll();
                });
        log.info("Finished deleting bills");
    }
}