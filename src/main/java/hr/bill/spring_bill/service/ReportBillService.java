package hr.bill.spring_bill.service;

import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.b2b.ReportBillRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportBillService {

    private final EposlovanjeClient eposlovanjeClient;
    private final EposlovanjeUtilClient eposlovanjeUtilClient;
    private final SupplierProperties supplierProperties;

    public List<DocumentStatusResponse> getBills() {
        throw new UnsupportedOperationException();
    }

    public DocumentStatusResponse createBill(ReportBillRequest request) {
        throw new UnsupportedOperationException();
    }

    public DocumentStatusResponse cancelBill(long originalId, int newBillId) {
        throw new UnsupportedOperationException();
    }

    public BillReviewResponse reviewBill(ReportBillRequest request) {
        throw new UnsupportedOperationException();
    }
}