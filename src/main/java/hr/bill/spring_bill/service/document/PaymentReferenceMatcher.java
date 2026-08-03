package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReferenceMatcher {

    private final BankTransactionRepository bankTransactionRepository;

    public Set<String> paidReferences(CreditDebitIndicator indicator, String receiverIban) {
        log.debug("Loading paid references for indicator {} and receiver IBAN {}", indicator, receiverIban);
        Set<String> references = bankTransactionRepository.findAllByCreditDebitIndicatorAndReceiverIbanIgnoreCase(indicator, receiverIban).stream()
                .map(BankTransactionEntity::getReference)
                .filter(Objects::nonNull)
                .map(PaymentReferenceMatcher::normalize)
                .collect(Collectors.toSet());
        log.debug("Found {} paid reference(s) for receiver IBAN {}", references.size(), receiverIban);
        return references;
    }

    public boolean isPaid(Set<String> paidReferences, String fullBillId) {
        return paidReferences.contains(normalize(HrPaymentReferenceService.buildReference(fullBillId)));
    }

    private static String normalize(String reference) {
        return reference.replace(" ", "").toUpperCase();
    }
}
