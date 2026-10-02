package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.model.enums.TenantPropety;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankStatementScheduler {

    private static final String ZONE = "Europe/Zagreb";

    private final BankStatementService bankStatementService;

    private final TenantService tenantService;

    private final TenantPropertyService tenantPropertyService;

    @Value("${bill.statement.mail-enabled}")
    private boolean mailEnabled;

    /** Imports the previous day's bank statement from the AIS API once that day is closed. */
    @Scheduled(cron = "${bill.schedule.bank-statement-cron}", zone = ZONE)
    public void importPreviousDay() {
        tenantService.forEachTenant("bank statement import", this::importPreviousDayForTenant);
    }

    private void importPreviousDayForTenant() {
        LocalDate yesterday = LocalDate.now(ZoneId.of(ZONE)).minusDays(1);
        log.info("Importing bank statement for {} from AIS", yesterday);
        List<BankStatementResponse> imported = bankStatementService.getTransactions(yesterday, yesterday);
        log.info("Finished importing bank statement for {}, {} statement(s) imported", yesterday, imported.size());

        if (!mailEnabled) {
            log.debug("Skipping statement email, bill.statement.mail-enabled is false");
            return;
        }
        Optional<String> mailTo = tenantPropertyService.find(TenantPropety.STATEMENT_MAIL_TO);
        if (mailTo.isEmpty()) {
            log.warn("Skipping statement email, bill.statement.mail-enabled is true but the tenant has no {} property",
                    TenantPropety.STATEMENT_MAIL_TO);
        } else if (!imported.isEmpty()) {
            bankStatementService.sendImportedStatements(imported, mailTo.get());
        }
    }
}
