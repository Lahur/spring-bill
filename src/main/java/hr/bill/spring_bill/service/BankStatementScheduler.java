package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dto.web.BankStatementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankStatementScheduler {

    private static final String ZONE = "Europe/Zagreb";

    private final BankStatementService bankStatementService;

    @Value("${bill.statement.mail-enabled}")
    private boolean mailEnabled;

    @Value("${bill.statement.mail-to}")
    private String mailTo;

    /** Imports the previous day's bank statement from the AIS API once that day is closed. */
    @Scheduled(cron = "${bill.schedule.bank-statement-cron}", zone = ZONE)
    public void importPreviousDay() {
        LocalDate yesterday = LocalDate.now(ZoneId.of(ZONE)).minusDays(1);
        log.info("Importing bank statement for {} from AIS", yesterday);
        List<BankStatementResponse> imported = bankStatementService.getTransactions(yesterday, yesterday);
        log.info("Finished importing bank statement for {}, {} statement(s) imported", yesterday, imported.size());

        if (!mailEnabled) {
            log.debug("Skipping statement email, bill.statement.mail-enabled is false");
        } else if (mailTo == null || mailTo.isBlank()) {
            log.warn("Skipping statement email, bill.statement.mail-enabled is true but bill.statement.mail-to is empty");
        } else if (!imported.isEmpty()) {
            bankStatementService.sendImportedStatements(imported, mailTo.trim());
        }
    }
}
