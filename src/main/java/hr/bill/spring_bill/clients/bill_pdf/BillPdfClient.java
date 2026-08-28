package hr.bill.spring_bill.clients.bill_pdf;

import hr.bill.spring_bill.dto.bill_pdf.request.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "bill-pdf",
        url = "${bill.hub-url}"
)
public interface BillPdfClient {

    @GetMapping("")
    String ping();

    @PostMapping("/reports/bill")
    byte[] renderBill(@RequestBody BillRequest req);

    @PostMapping("/reports/bill-with-details")
    byte[] renderBillWithDetails(@RequestBody BillWithDetailsRequest req);

    @PostMapping("/reports/incoming-invoice")
    byte[] renderIncomingInvoice(@RequestBody IncomingInvoiceRequest req);

    @PostMapping("/reports/pos-transaction")
    byte[] renderPosTransaction(@RequestBody PosTransactionRequest req);

    @PostMapping("/reports/disbursement")
    byte[] renderDisbursement(@RequestBody DisbursementRequest req);

    @PostMapping("/reports/deposit")
    byte[] renderDeposit(@RequestBody DepositRequest req);
}
