package hr.bill.spring_bill.clients.f1_web;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PrintFormat;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.ChangePaymentMethodDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.CreateReceiptDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.GetReceiptsQuery;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.UpdateReceiptDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "f1-web",
        url = "${bill.f1-web.base-url}",
        configuration = F1WebClientConfig.class
)
public interface F1WebClient {

    // ── Receipts ──────────────────────────────────────────────────────────────

    @GetMapping("/api/Receipts")
    ReceiptListResultDto getReceipts(@SpringQueryMap GetReceiptsQuery query);

    @PostMapping("/api/Receipts")
    ReceiptDto createReceipt(@RequestBody CreateReceiptDto req);

    @GetMapping("/api/Receipts/{id}")
    ReceiptDto getReceipt(@PathVariable("id") int id);

    @PutMapping("/api/Receipts/{id}")
    ReceiptDto updateReceipt(@PathVariable("id") int id, @RequestBody UpdateReceiptDto req);

    @GetMapping("/api/Receipts/by-status/{status}")
    List<ReceiptSummaryDto> getReceiptsByStatus(@PathVariable("status") FiscalStatus status);

    @GetMapping("/api/Receipts/by-date-range")
    List<ReceiptSummaryDto> getReceiptsByDateRange(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate);

    @GetMapping("/api/Receipts/statistics")
    ReceiptStatisticsDto getStatistics();

    @GetMapping("/api/Receipts/pending-retry")
    List<ReceiptSummaryDto> getPendingRetry(
            @RequestParam(value = "maxRetryCount", required = false) Integer maxRetryCount);

    @PostMapping("/api/Receipts/create-and-fiscalize")
    FiscalizationResultDto createAndFiscalize(@RequestBody CreateReceiptDto req);

    @PostMapping("/api/Receipts/{id}/fiscalize")
    FiscalizationResultDto fiscalize(@PathVariable("id") int id);

    @PostMapping("/api/Receipts/{id}/retry-fiscalization")
    FiscalizationResultDto retryFiscalization(@PathVariable("id") int id);

    @PostMapping("/api/Receipts/{id}/change-payment-method")
    FiscalizationResultDto changePaymentMethod(
            @PathVariable("id") int id,
            @RequestBody ChangePaymentMethodDto req);

    @GetMapping("/api/Receipts/{id}/fiscalization-readiness")
    FiscalizationReadinessDto getFiscalizationReadiness(@PathVariable("id") int id);

    @PostMapping("/api/Receipts/{id}/storno")
    FiscalizationResultDto storno(@PathVariable("id") int id);

    @GetMapping("/api/Receipts/{id}/credit-note-readiness")
    CreditNoteReadinessDto getCreditNoteReadiness(@PathVariable("id") int id);

    // ── PDF downloads ─────────────────────────────────────────────────────────

    @GetMapping("/api/Receipts/{id}/pdf")
    byte[] getReceiptPdf(
            @PathVariable("id") int id,
            @RequestParam(value = "format", required = false) PrintFormat format);

    @GetMapping("/api/Receipts/{id}/pdf/a4")
    byte[] getReceiptPdfA4(@PathVariable("id") int id);

    @GetMapping("/api/Receipts/{id}/pdf-copy")
    byte[] getReceiptPdfCopy(
            @PathVariable("id") int id,
            @RequestParam(value = "copyNumber", required = false) Integer copyNumber);

    @GetMapping("/api/Receipts/summary-report")
    byte[] getSummaryReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "reportTitle", required = false) String reportTitle);

    // ── Validation ────────────────────────────────────────────────────────────

    @PostMapping("/api/Receipts/validate")
    ValidationResultDto validateReceipt(@RequestBody CreateReceiptDto req);
}
