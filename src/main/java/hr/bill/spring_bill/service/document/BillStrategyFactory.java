package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BillStrategyFactory {

    private final Map<BillReportType, BillStrategy> strategies;

    public BillStrategyFactory(List<BillStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(BillStrategy::getType, Function.identity()));
    }

    public BillDocument createDocument(BillReportType type, String id) {
        return getStrategy(type).createDocument(id);
    }

    public void incrementSentCount(BillReportType type, String id) {
        getStrategy(type).incrementSentCount(id);
    }

    public int markPaidFromBankStatement(CamtDocument statement) {
        return strategies.values().stream()
                .mapToInt(strategy -> strategy.markPaidFromBankStatement(statement))
                .sum();
    }

    private BillStrategy getStrategy(BillReportType type) {
        BillStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No document strategy registered for report type: " + type);
        }
        return strategy;
    }
}