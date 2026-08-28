package hr.bill.spring_bill.clients.eposlovanje_util;

import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.PaymentInfo;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.request.FiscalizeDocumentRequestDTO;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.request.SendSmsMessageDTO;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "eposlovanje-util",
        url = "${bill.pondi.base-url}",
        configuration = EposlovanjeUtilClientConfig.class
)
public interface EposlovanjeUtilClient {

    // ── Barcode ───────────────────────────────────────────────────────────────

    /** Generates PDF417 barcode (HRVHUB30); base64 PNG returned in response.message */
    @PostMapping("/barcode/pdf417")
    ApiResponse generatePdf417(@RequestBody PaymentInfo req);

    /** Generates QR barcode (EPC); base64 PNG returned in response.message */
    @PostMapping("/barcode/qr")
    ApiResponse generateQr(@RequestBody PaymentInfo req);

    // ── Business Entities ─────────────────────────────────────────────────────

    /** Returns first 10 entity names matching the query */
    @GetMapping("/business-entities/find/{name}")
    List<String> findBusinessEntities(@PathVariable("name") String name);

    /** Returns null / throws FeignException.NotFound when subject not found */
    @GetMapping("/business-entities/oib/{oib}")
    BusinessEntity getBusinessEntityByOib(@PathVariable("oib") String oib);

    @GetMapping("/business-entities/name/{name}")
    BusinessEntity getBusinessEntityByName(@PathVariable("name") String name);

    // ── Business Units ────────────────────────────────────────────────────────

    @GetMapping("/business-units/{oib}")
    List<BusinessUnitDTO> getBusinessUnits(@PathVariable("oib") String oib);

    // ── Fiscalization F1 ──────────────────────────────────────────────────────

    @PostMapping("/fiscalization/f1/fiscalize")
    FiscalizeDocumentResponseDTO fiscalizeF1(@RequestBody FiscalizeDocumentRequestDTO req);

    // ── Postoffices ───────────────────────────────────────────────────────────

    @GetMapping("/postoffices/zip/{zip}")
    List<Postoffice> getPostofficesByZip(@PathVariable("zip") String zip);

    @GetMapping("/postoffices/name/{name}")
    List<Postoffice> getPostofficesByName(@PathVariable("name") String name);

    // ── SMS ───────────────────────────────────────────────────────────────────

    @PostMapping("/sms/send")
    SendSmsMessageResponseDTO sendSms(@RequestBody SendSmsMessageDTO req);

    /** POST with query params only — no body. From/To are ISO-8601 datetime strings. */
    @PostMapping("/sms/outbox")
    List<SmsMessageDTO> getSmsOutbox(
            @RequestParam(value = "From", required = false) String from,
            @RequestParam(value = "To", required = false) String to);
}
