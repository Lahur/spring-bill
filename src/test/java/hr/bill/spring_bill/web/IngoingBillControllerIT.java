package hr.bill.spring_bill.web;

import hr.bill.spring_bill.AbstractIntegrationTest;
import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentListParams;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatus;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.service.UblXmlService;
import hr.bill.spring_bill.xml.ubl.model.UblAdditionalDocumentReference;
import hr.bill.spring_bill.xml.ubl.model.UblInvoice;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Drives {@link IngoingBillController} over HTTP. Unlike the F1 family, {@code EposlovanjeClient}
 * is a real client whose {@code /eposlovanje} route proxies straight through to Eposlovanje's
 * sandbox (not a fake) — and there's no create endpoint here at all (ingoing bills only ever come
 * from syncing real purchases). So detail/mark-paid tests below look up a real document that
 * already exists in the sandbox rather than creating one, and skip themselves (rather than fail)
 * if the sandbox currently has none — that's sandbox state outside this repo's control. */
class IngoingBillControllerIT extends AbstractIntegrationTest {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private EposlovanjeClient eposlovanjeClient;

    @Autowired
    private UblXmlService ublXmlService;

    @Test
    void listsLocallyPersistedBills() throws Exception {
        BillEntity seeded = billRepository.save(BillEntity.builder()
                .systemId(System.nanoTime())
                .fullBillId(UUID.randomUUID().toString())
                .clientName("Test Supplier d.o.o.")
                .billDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("42.00"))
                .billType(BillType.INGOING_BILL)
                .build());

        BillResponse[] listed = objectMapper.readValue(mockMvc.perform(get("/bill/ingoing").with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillResponse[].class);

        assertThat(Arrays.stream(listed).map(BillResponse::id)).contains(seeded.getId());
    }

    @Test
    void filtersBillsFromTheUpstreamSandbox() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "dateFrom", LocalDate.now().minusYears(2).toString()));

        mockMvc.perform(post("/bill/ingoing/filter")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void fetchesInfoForARealUpstreamDocument() throws Exception {
        DocumentStatusResponse existing = findAnyIncomingDocument()
                .orElse(null);
        Assumptions.assumeTrue(existing != null,
                "No incoming documents currently exist in the Eposlovanje sandbox to test against");

        BillInfoResponse info = objectMapper.readValue(mockMvc.perform(get("/bill/ingoing/" + existing.id()).with(jwt()))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsByteArray(), BillInfoResponse.class);
        assertThat(info.mainDataInfo()).isNotNull();
    }

    @Test
    void generatesPdf417ForARealUpstreamDocument() throws Exception {
        DocumentStatusResponse existing = findAnyIncomingDocument().orElse(null);
        Assumptions.assumeTrue(existing != null,
                "No incoming documents currently exist in the Eposlovanje sandbox to test against");

        mockMvc.perform(get("/bill/ingoing/" + existing.id() + "/pdf417").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void marksAnUnpaidUpstreamDocumentAsPaid() throws Exception {
        DocumentStatusResponse unpaid = findAnyIncomingDocument()
                .filter(d -> d.status() != DocumentStatus.PlacenUPotpunosti)
                .orElse(null);
        Assumptions.assumeTrue(unpaid != null,
                "No unpaid incoming document currently exists in the Eposlovanje sandbox to test against");
        billRepository.save(BillEntity.builder()
                .systemId(unpaid.id())
                .fullBillId(unpaid.documentId())
                .clientName(unpaid.supplierPartyName())
                .billDate(LocalDateTime.parse(unpaid.issuedOn()))
                .totalAmount(BigDecimal.valueOf(unpaid.amount()))
                .billType(BillType.INGOING_BILL)
                .build());

        mockMvc.perform(post("/bill/ingoing/" + unpaid.id() + "/mark-as-paid").with(jwt()))
                .andExpect(status().isOk());

        assertThat(billRepository.findBySystemIdAndBillType(unpaid.id(), BillType.INGOING_BILL))
                .hasValueSatisfying(b -> assertThat(b.getDocumentStatus())
                        .isEqualTo(BillDocumentStatus.PlacenUPotpunosti));
    }

    @Test
    void fullRefreshSyncsCurrentMonthBillsFromTheUpstreamSandbox() throws Exception {
        int refreshed = Integer.parseInt(mockMvc.perform(post("/bill/ingoing/full-refresh").with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(refreshed).isNotNegative();
        assertThat(billRepository.findAllByBillTypeOrderByBillDateDesc(BillType.INGOING_BILL)).hasSizeGreaterThanOrEqualTo(refreshed);

        // Running it again must update the existing rows instead of violating the unique constraint.
        mockMvc.perform(post("/bill/ingoing/full-refresh").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void sendingABillMissingLocallyStillCountsTheOthers() throws Exception {
        List<DocumentStatusResponse> documents = findIncomingDocuments().stream()
                .filter(this::isRenderable)
                .limit(2)
                .toList();
        Assumptions.assumeTrue(documents.size() >= 2,
                "Fewer than two renderable incoming documents currently exist in the Eposlovanje sandbox to test against");
        DocumentStatusResponse missingLocally = documents.get(0);
        DocumentStatusResponse storedLocally = documents.get(1);
        billRepository.findBySystemIdAndBillType(missingLocally.id(), BillType.INGOING_BILL)
                .ifPresent(billRepository::delete);
        BillEntity stored = billRepository.findBySystemIdAndBillType(storedLocally.id(), BillType.INGOING_BILL)
                .orElseGet(() -> billRepository.save(BillEntity.builder()
                        .systemId(storedLocally.id())
                        .fullBillId(storedLocally.documentId())
                        .clientName(storedLocally.supplierPartyName())
                        .billDate(LocalDateTime.parse(storedLocally.issuedOn()))
                        .totalAmount(BigDecimal.valueOf(storedLocally.amount()))
                        .billType(BillType.INGOING_BILL)
                        .build()));
        int sentCountBefore = stored.getSentCount();

        // The missing bill goes first: before the fix its NotFoundException aborted the loop, so the
        // stored bill's sent count was never incremented and the request 404'd after the mail went out.
        String requestBody = objectMapper.writeValueAsString(SendBillReportsRequest.builder()
                .reports(List.of(reportItem(missingLocally), reportItem(storedLocally)))
                .email("ingoing-sent-count@example.com")
                .build());

        mockMvc.perform(post("/document/generate-and-send")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()))
                .andExpect(status().isOk());

        assertThat(billRepository.findBySystemIdAndBillType(storedLocally.id(), BillType.INGOING_BILL))
                .hasValueSatisfying(bill -> assertThat(bill.getSentCount()).isEqualTo(sentCountBefore + 1));
        assertThat(billRepository.findBySystemIdAndBillType(missingLocally.id(), BillType.INGOING_BILL)).isEmpty();
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/bill/ingoing")).andExpect(status().isUnauthorized());
    }

    private Optional<DocumentStatusResponse> findAnyIncomingDocument() {
        return findIncomingDocuments().stream().findFirst();
    }

    private List<DocumentStatusResponse> findIncomingDocuments() {
        return eposlovanjeClient.getIncomingDocuments(DocumentListParams.builder()
                .issuedFrom(LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ISO_DATE_TIME))
                .limit(5)
                .build());
    }

    /** The sandbox also holds invoices other test runs sent with a stub "%PDF-1.4" attachment, which
     * PDFBox can't merge; only documents with no embedded PDF (hub-bill renders those) or a loadable
     * one can be sent. */
    private boolean isRenderable(DocumentStatusResponse document) {
        UblInvoice invoice = ublXmlService.parse(eposlovanjeClient.getDocument(document.id()).document());
        Optional<String> embedded = Optional.ofNullable(invoice.getAdditionalDocumentReferences()).stream()
                .flatMap(List::stream)
                .map(UblAdditionalDocumentReference::getAttachment)
                .filter(Objects::nonNull)
                .map(UblAdditionalDocumentReference.Attachment::getEmbeddedDocumentBinaryObject)
                .filter(Objects::nonNull)
                .map(UblAdditionalDocumentReference.Attachment.EmbeddedDocumentBinaryObject::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
        if (embedded.isEmpty()) {
            return true;
        }
        try (PDDocument ignored = Loader.loadPDF(Base64.getDecoder().decode(embedded.get().trim()))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static SendBillReportItem reportItem(DocumentStatusResponse document) {
        return SendBillReportItem.builder()
                .id(String.valueOf(document.id()))
                .billId(document.documentId())
                .type(BillReportType.INGOING)
                .build();
    }
}
