package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Re-checks which of a strategy's bills were paid, against already imported bank statements. Mirrors
 * what {@code BankStatementImportService} does when a statement is imported (minus the upstream
 * fallback, since the bills are freshly synced), plus the overlay {@code getBillsFilter} applies for
 * bills whose system id a bank transaction is already booked against.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankStatementPaymentSync {

    private final BankTransactionRepository bankTransactionRepository;

    private final BillRepository billRepository;

    private final PaymentReferenceMatcher paymentReferenceMatcher;

    private final SupplierProperties supplierProperties;

    /**
     * @return number of bills of {@code billType} issued from {@code from} onwards that were marked as paid
     */
    public int sync(BillStrategy strategy, BillType billType, LocalDate from) {
        List<BankTransactionEntity> transactions = bankTransactionRepository
                .findAllByCreditDebitIndicatorAndReceiverIbanIgnoreCaseAndTransactionDateGreaterThanEqual(
                        CreditDebitIndicator.CRDT, supplierProperties.iban(), from.atStartOfDay());
        log.debug("Checking {} bill(s) against {} bank transaction(s) since {}", billType, transactions.size(), from);

        int alreadyBooked = markBillsWithBookedTransactions(billType, from);

        List<BankTransactionEntity> unresolved = transactions.stream()
                .filter(tx -> tx.getBillSystemId() == null)
                .toList();
        int matched = strategy.markPaidFromBankStatement(unresolved);
        if (matched > 0) {
            bankTransactionRepository.saveAll(unresolved);
        }
        log.info("{} bank statement sync since {}: {} bill(s) already booked, {} matched by payment reference",
                billType, from, alreadyBooked, matched);
        return alreadyBooked + matched;
    }

    private int markBillsWithBookedTransactions(BillType billType, LocalDate from) {
        List<BillEntity> bills = billRepository.findAllByBillTypeOrderByBillDateDesc(billType).stream()
                .filter(bill -> !bill.getBillDate().toLocalDate().isBefore(from))
                .toList();
        Set<String> paidSystemIds = paymentReferenceMatcher.paidBillSystemIds(bills.stream()
                .map(bill -> String.valueOf(bill.getSystemId()))
                .toList());
        List<BillEntity> toMark = bills.stream()
                .filter(bill -> paidSystemIds.contains(String.valueOf(bill.getSystemId())))
                .filter(bill -> bill.getDocumentStatus() != BillDocumentStatus.PlacenUPotpunosti)
                .peek(bill -> bill.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti))
                .toList();
        billRepository.saveAll(toMark);
        return toMark.size();
    }
}
