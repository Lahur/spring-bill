package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentReferenceMatcher {

    private final BankTransactionRepository bankTransactionRepository;

    public Set<String> paidReferences(CreditDebitIndicator indicator, String receiverIban) {
        return bankTransactionRepository.findAllByCreditDebitIndicatorAndReceiverIbanIgnoreCase(indicator, receiverIban).stream()
                .map(BankTransactionEntity::getReference)
                .filter(Objects::nonNull)
                .map(PaymentReferenceMatcher::normalize)
                .collect(Collectors.toSet());
    }

    public boolean isPaid(Set<String> paidReferences, String fullBillId) {
        return paidReferences.contains(normalize(HrPaymentReferenceService.buildReference(fullBillId)));
    }

    private static String normalize(String reference) {
        return reference.replace(" ", "").toUpperCase();
    }
}
