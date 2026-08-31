package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
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
        Set<String> references = bankTransactionRepository.findAllByCreditDebitIndicatorAndReceiverIbanIgnoreCaseOrderByTransactionDateDesc(indicator, receiverIban).stream()
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

    /**
     * Of the given bill system ids, returns those referenced by the {@code billSystemId} of at
     * least one bank transaction (i.e. bills a matched bank payment has been booked against).
     */
    public Set<String> paidBillSystemIds(Collection<String> billSystemIds) {
        if (billSystemIds.isEmpty()) {
            return Set.of();
        }
        Set<String> paid = bankTransactionRepository.findAllByBillSystemIdIn(billSystemIds).stream()
                .map(BankTransactionEntity::getBillSystemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.debug("Found {} of {} bill(s) paid by a matched bank transaction", paid.size(), billSystemIds.size());
        return paid;
    }

    /**
     * Returns the bills unchanged, except that any bill whose system id is referenced by the
     * {@code billSystemId} of at least one bank transaction is marked as paid in full.
     */
    public List<BillResponse> markPaidByBillSystemId(List<BillResponse> bills) {
        Set<String> systemIds = bills.stream()
                .map(BillResponse::systemId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        Set<String> paidSystemIds = paidBillSystemIds(systemIds);
        if (paidSystemIds.isEmpty()) {
            return bills;
        }
        return bills.stream()
                .map(bill -> bill.systemId() != null && paidSystemIds.contains(String.valueOf(bill.systemId()))
                        ? bill.toBuilder().documentStatus(BillDocumentStatus.PlacenUPotpunosti).build()
                        : bill)
                .toList();
    }

    private static String normalize(String reference) {
        return reference.replace(" ", "").toUpperCase();
    }
}
