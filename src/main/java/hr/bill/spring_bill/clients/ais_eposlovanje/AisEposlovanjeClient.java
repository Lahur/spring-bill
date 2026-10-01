package hr.bill.spring_bill.clients.ais_eposlovanje;

import hr.bill.spring_bill.dto.ais_eposlovanje.request.RequisitionCreateRequest;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.AccountDetailsResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.AccountResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.BalancesResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.InstitutionResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.MeResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.RequisitionListResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.RequisitionResponse;
import hr.bill.spring_bill.dto.ais_eposlovanje.response.TransactionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Open banking / AIS API (ais.eposlovanje.hr) — bank account linking (requisitions)
 * and read-only access to the accounts a client has approved.
 * <p>
 * Institutions and requisitions (steps 1–5 of the flow) are not subject to the
 * per-account quota; {@code /accounts/{id}/details}, {@code /balances} and
 * {@code /transactions} go to the bank and are, typically 4 calls/day/account.
 */
@FeignClient(
        name = "ais-eposlovanje",
        url = "${bill.ais-eposlovanje.base-url}",
        configuration = AisEposlovanjeClientConfig.class
)
public interface AisEposlovanjeClient {

    // ── Me ────────────────────────────────────────────────────────────────────

    @GetMapping("/api/v2/me")
    MeResponse getMe();

    // ── Institutions ─────────────────────────────────────────────────────────

    @GetMapping("/api/v2/institutions")
    List<InstitutionResponse> getInstitutions(@RequestParam(value = "country", required = false) String country);

    @GetMapping("/api/v2/institutions/{id}")
    InstitutionResponse getInstitution(@PathVariable("id") String id);

    // ── Requisitions ──────────────────────────────────────────────────────────

    @PostMapping("/api/v2/requisitions")
    RequisitionResponse createRequisition(@RequestBody RequisitionCreateRequest req);

    @GetMapping("/api/v2/requisitions")
    RequisitionListResponse getRequisitions();

    @GetMapping("/api/v2/requisitions/{id}")
    RequisitionResponse getRequisition(@PathVariable("id") String id);

    @DeleteMapping("/api/v2/requisitions/{id}")
    void deleteRequisition(@PathVariable("id") String id);

    // ── Accounts ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v2/accounts/{id}")
    AccountResponse getAccount(@PathVariable("id") String id);

    @GetMapping("/api/v2/accounts/{id}/details")
    AccountDetailsResponse getAccountDetails(@PathVariable("id") String id);

    @GetMapping("/api/v2/accounts/{id}/balances")
    BalancesResponse getAccountBalances(@PathVariable("id") String id);

    @GetMapping("/api/v2/accounts/{id}/transactions")
    TransactionsResponse getAccountTransactions(
            @PathVariable("id") String id,
            @RequestParam(value = "date_from", required = false) String dateFrom,
            @RequestParam(value = "date_to", required = false) String dateTo);
}
