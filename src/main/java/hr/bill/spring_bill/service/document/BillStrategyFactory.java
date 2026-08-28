package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BillStrategyFactory extends DocumentStrategyFactory {

    private final Map<BillReportType, BillStrategy> billStrategies;

    public BillStrategyFactory(List<BillStrategy> strategies) {
        super(new ArrayList<DocumentStrategy>(strategies));
        this.billStrategies = strategies.stream()
                .collect(Collectors.toMap(BillStrategy::getType, Function.identity()));
    }

    public int markPaidFromBankStatement(CamtDocument statement) {
        log.debug("Marking bills paid from bank statement across {} strategies", billStrategies.size());
        int matched = billStrategies.values().stream()
                .mapToInt(strategy -> strategy.markPaidFromBankStatement(statement))
                .sum();
        log.debug("Matched {} bill(s) as paid across all strategies", matched);
        return matched;
    }
}
