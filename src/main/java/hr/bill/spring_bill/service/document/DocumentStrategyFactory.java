package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.BillReportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentStrategyFactory {

    private final Map<BillReportType, DocumentStrategy> strategies;

    public DocumentStrategyFactory(List<DocumentStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(DocumentStrategy::getType, Function.identity()));
    }

    public BillDocument createDocument(BillReportType type, String id) {
        log.debug("Creating document for type {} id {}", type, id);
        return getStrategy(type).createDocument(id);
    }

    public void incrementSentCount(BillReportType type, String id) {
        log.debug("Incrementing sent count for type {} id {}", type, id);
        getStrategy(type).incrementSentCount(id);
    }

    private DocumentStrategy getStrategy(BillReportType type) {
        DocumentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No document strategy registered for report type: " + type);
        }
        return strategy;
    }
}
