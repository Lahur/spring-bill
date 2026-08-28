package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillStrategy extends DocumentStrategy {

    List<BillResponse> getBills();

    PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart);

    Optional<LocalDate> findEarliestBillMonth();

    BillResponse createBill(BaseBillRequest request);

    BillInfoResponse getBillInfo(String id);

    BillResponse cancel(String originalId, String newId);

    BillReviewResponse reviewBill(BaseBillRequest request);

    void sync();

    void deleteAll();

    int markPaidFromBankStatement(CamtDocument statement);
}