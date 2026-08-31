package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.BillSearchParams;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillStrategy extends DocumentStrategy {

    List<BillResponse> getBills();

    List<BillResponse> getBillsFilter(BillSearchParams params);

    PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart);

    Optional<LocalDate> findEarliestBillMonth();

    BillResponse createBill(BaseBillRequest request);

    BillInfoResponse getBillInfo(String id);

    BillResponse cancel(String originalId, String newId);

    BillReviewResponse reviewBill(BaseBillRequest request);

    void sync();

    void deleteAll();

    /**
     * Matches persisted bank transactions against this strategy's bills. Matched bills are marked
     * paid and the matched transaction's {@code billSystemId} is set to the bill's system id.
     *
     * @return number of bills marked as paid
     */
    int markPaidFromBankStatement(List<BankTransactionEntity> transactions);

    default String billSystemId(BillEntity bill) {
        return bill.getSystemId() == null ? null : String.valueOf(bill.getSystemId());
    }
}