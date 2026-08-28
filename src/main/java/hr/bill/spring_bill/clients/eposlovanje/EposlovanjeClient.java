package hr.bill.spring_bill.clients.eposlovanje;

import hr.bill.spring_bill.clients.eposlovanje.params.BankingTransactionParams;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentListParams;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentStatusLookupParams;
import hr.bill.spring_bill.clients.eposlovanje.params.EReportingRequestsParams;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.F2Settings;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.PingResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request.*;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "eposlovanje",
        url = "${bill.eposlovanje.base-url}",
        configuration = EposlovanjeClientConfig.class
)
public interface EposlovanjeClient {

    // ── Ping ─────────────────────────────────────────────────────────────────

    @GetMapping("/api/v2/ping")
    PingResponse ping();

    // ── Documents ─────────────────────────────────────────────────────────────

    @PostMapping("/api/v2/document/send")
    DocumentSendResponse sendDocument(@RequestBody DocumentSendRequest req);

    @GetMapping("/api/v2/document/get/{id}")
    DocumentGetResponse getDocument(@PathVariable("id") long id);

    @GetMapping("/api/v2/document/visualization/{id}")
    DocumentVisualizationResponse getVisualization(@PathVariable("id") long id);

    @GetMapping("/api/v2/document/incoming")
    List<DocumentStatusResponse> getIncomingDocuments(@SpringQueryMap DocumentListParams params);

    @GetMapping("/api/v2/document/outgoing")
    List<DocumentStatusResponse> getOutgoingDocuments(@SpringQueryMap DocumentListParams params);

    @GetMapping("/api/v2/document/status/{id}")
    DocumentStatusResponse getDocumentStatus(@PathVariable("id") long id);

    @GetMapping("/api/v2/document/statuslookup")
    DocumentStatusResponse getDocumentStatusLookup(@SpringQueryMap DocumentStatusLookupParams params);

    @PostMapping("/api/v2/document/changestatus/{id}")
    DocumentChangeStatusResponse changeDocumentStatus(
            @PathVariable("id") long id,
            @RequestBody DocumentChangeStatusRequest req);

    @PostMapping("/api/v2/document/validate")
    DocumentValidateResponse validateDocument(@RequestBody DocumentValidateRequest req);

    // ── eReporting ────────────────────────────────────────────────────────────

    @PostMapping("/api/v2/ereporting/fiscalize/{id}")
    void fiscalize(@PathVariable("id") long id);

    @PostMapping("/api/v2/ereporting/reportdocument")
    EReportingReportDocumentResponse reportDocument(@RequestBody EReportingReportDocumentRequest req);

    @PostMapping("/api/v2/ereporting/paid/{id}")
    void paidById(@PathVariable("id") long id, @RequestBody EReportingDocumentPaidRequest req);

    @PostMapping("/api/v2/ereporting/paid")
    void paid(@RequestBody EReportingDocumentPaidRequest req);

    @PostMapping("/api/v2/ereporting/rejected/{id}")
    void rejectedById(@PathVariable("id") long id, @RequestBody EReportingDocumentRejectedRequest req);

    @PostMapping("/api/v2/ereporting/rejected")
    void rejected(@RequestBody EReportingDocumentRejectedRequest req);

    @GetMapping("/api/v2/ereporting/requests")
    EReportingRequestsResponse getEReportingRequests(@SpringQueryMap EReportingRequestsParams params);

    // ── Banking ───────────────────────────────────────────────────────────────

    @GetMapping("/api/v2/banking/accounts")
    List<BankingAccount> getBankingAccounts();

    @GetMapping("/api/v2/banking/transactions")
    List<BankingTransaction> getBankingTransactions(@SpringQueryMap BankingTransactionParams params);

    // ── Account ───────────────────────────────────────────────────────────────

    @GetMapping("/api/v2/account/balance")
    AccountBalanceResponse getBalance();

    @PostMapping("/api/v2/account/transferprepaidfunds")
    AccountTransferPrepaidFundsResponse transferPrepaidFunds(@RequestBody AccountTransferPrepaidFundsRequest req);

    @PostMapping("/api/v2/account/apikey")
    AccountApiKeyResponse getApiKey(@RequestBody AccountApiKeyRequest req);

    @GetMapping("/api/v2/account/f2settings")
    F2Settings getF2Settings();

    @PutMapping("/api/v2/account/updatef2settings")
    void updateF2Settings(@RequestBody F2Settings settings);

    @PostMapping("/api/v2/account/register/{erptag}")
    AccountRegistrationResponse register(
            @PathVariable("erptag") String erptag,
            @RequestBody AccountRegistrationRequest req);

    // ── Business units ────────────────────────────────────────────────────────

    @GetMapping("/api/v2/businessunits/{vatid}")
    List<BusinessUnit> getBusinessUnits(@PathVariable("vatid") String vatid);

    // ── AMS ───────────────────────────────────────────────────────────────────

    @PostMapping("/api/v2/ams/check")
    AmsCheckResponse amsCheck(@RequestBody AmsCheckRequest req);
}
