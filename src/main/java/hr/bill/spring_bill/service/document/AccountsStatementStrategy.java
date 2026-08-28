package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dao.AccountsStatementRepository;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.model.AccountsStatementEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountsStatementStrategy implements DocumentStrategy {

    private final AccountsStatementRepository accountsStatementRepository;

    @Override
    public BillReportType getType() {
        return BillReportType.ACCOUNTS_STATEMENT;
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for accounts statement {}", id);
        AccountsStatementEntity entity = accountsStatementRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Accounts statement not found for id: " + id));
        if (entity.getBillPath() == null) {
            throw new NotFoundException("No bill PDF uploaded for accounts statement: " + id);
        }
        try {
            return BillDocument.builder()
                    .content(Files.readAllBytes(Path.of(entity.getBillPath())))
                    .filename("accounts-statement-" + entity.getId())
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for accounts statement {}", id);
        AccountsStatementEntity entity = accountsStatementRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Accounts statement not found for id: " + id));
        entity.setSentCount(entity.getSentCount() + 1);
        accountsStatementRepository.save(entity);
    }
}
