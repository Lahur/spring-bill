package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentListParams;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.bill_pdf.request.BillWithDetailsRequest;
import hr.bill.spring_bill.dto.bill_pdf.request.IncomingInvoiceRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common.DocumentStatus;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request.DocumentChangeStatusRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentGetResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.ApiResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.BillEntityMapper;
import hr.bill.spring_bill.mapper.BillInfoMapper;
import hr.bill.spring_bill.mapper.CamtStatementMapper;
import hr.bill.spring_bill.mapper.PaymentInfoMapper;
import hr.bill.spring_bill.mapper.UblInvoiceMapper;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.service.UblXmlService;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import hr.bill.spring_bill.xml.ubl.model.UblAdditionalDocumentReference;
import hr.bill.spring_bill.xml.ubl.model.UblInvoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngoingStrategy implements BillStrategy {

    private final EposlovanjeClient eposlovanjeClient;

    private final EposlovanjeUtilClient eposlovanjeUtilClient;

    private final BillPdfClient billPdfClient;

    private final UblXmlService ublXmlService;

    private final SupplierProperties supplierProperties;

    @Value("${bill.schedule.sync-lookback-weeks}")
    private long syncLookbackWeeks;

    private final PaymentInfoMapper paymentInfoMapper;

    private final UblInvoiceMapper ublInvoiceMapper;

    private final BillEntityMapper billEntityMapper;

    private final BillInfoMapper billInfoMapper;

    private final CamtStatementMapper camtStatementMapper;

    private final BillRepository repository;

    @Override
    public BillReportType getType() {
        return BillReportType.INGOING;
    }

    @Override
    public List<BillResponse> getBills() {
        List<BillEntity> bills = repository.findAllByBillTypeOrderByBillDateDesc(BillType.INGOING_BILL);
        return billEntityMapper.toBillResponseList(bills);
    }

    @Override
    public PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart) {
        DocumentListParams params = DocumentListParams.builder()
                .issuedFrom(monthStart.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .issuedTo(monthStart.plusMonths(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal unpaid = BigDecimal.ZERO;
        for (DocumentStatusResponse d : eposlovanjeClient.getIncomingDocuments(params)) {
            BigDecimal amount = BigDecimal.valueOf(d.amount());
            if (d.status() == DocumentStatus.PlacenUPotpunosti) {
                paid = paid.add(amount);
            } else {
                unpaid = unpaid.add(amount);
            }
        }
        return new PaidUnpaidTotals(paid, unpaid);
    }

    @Override
    public Optional<LocalDate> findEarliestBillMonth() {
        return eposlovanjeClient.getIncomingDocuments(DocumentListParams.builder().build()).stream()
                .map(d -> LocalDateTime.parse(d.issuedOn()))
                .min(LocalDateTime::compareTo)
                .map(dt -> dt.toLocalDate().withDayOfMonth(1));
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for ingoing bill {}", id);
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(Long.parseLong(id));
        UblInvoice ublInvoice = ublXmlService.parse(documentResponse.document());
        List<UblAdditionalDocumentReference> attachments = ublInvoice.getAdditionalDocumentReferences();
        if (attachments != null && !attachments.isEmpty()) {
            String embeddedContent = attachments.getFirst().getAttachment().getEmbeddedDocumentBinaryObject().getValue();
            if (embeddedContent != null && !embeddedContent.isBlank()) {
                return BillDocument.builder()
                        .content(Base64.getDecoder().decode(embeddedContent.trim()))
                        .filename(ublInvoice.getId().replace("/", "-").replace("\\", "-"))
                        .build();
            }
        }
        IncomingInvoiceRequest incomingInvoiceRequest = ublInvoiceMapper.toIncomingInvoiceRequest(ublInvoice);
        return BillDocument.builder()
                .content(billPdfClient.renderIncomingInvoice(incomingInvoiceRequest))
                .filename(ublInvoice.getId().replace("/", "-").replace("\\", "-"))
                .build();
    }

    @Override
    public BillResponse createBill(BaseBillRequest request) {
        return null;
    }

    public Optional<String> generatePdf417Ingoing(String id) {
        log.debug("Generating PDF417 for ingoing bill {}", id);
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(Long.parseLong(id));
        UblInvoice ublInvoice = ublXmlService.parse(documentResponse.document());
        return generatePdf417(ublInvoice);
    }

    private Optional<String> generatePdf417(UblInvoice ublInvoice) {
        ApiResponse apiResponse = eposlovanjeUtilClient.generatePdf417(paymentInfoMapper.toPaymentInfo(supplierProperties, ublInvoice));
        return Optional.ofNullable(apiResponse.message());
    }

    @Override
    public BillInfoResponse getBillInfo(String id) {
        log.debug("Fetching bill info for ingoing bill {}", id);
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(Long.parseLong(id));
        UblInvoice ublInvoice = ublXmlService.parse(documentResponse.document());
        return billInfoMapper.toBillInfoResponse(ublInvoice);
    }

    @Override
    public BillResponse cancel(String originalId, String newId) {
        return null;
    }

    public void markDocumentAsPaid(String id) {
        log.info("Marking ingoing bill {} as paid", id);
        eposlovanjeClient.changeDocumentStatus(Long.parseLong(id), DocumentChangeStatusRequest.builder()
                .status(DocumentStatus.PlacenUPotpunosti.getValue())
                .changedOn(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")))
                .build());
        BillEntity billEntity = repository.findBySystemIdAndBillType(Long.parseLong(id), BillType.INGOING_BILL)
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        billEntity.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti);
        repository.save(billEntity);
    }

    @Override
    public BillReviewResponse reviewBill(BaseBillRequest request) {
        return null;
    }

    @Override
    public void sync() {
        log.debug("Syncing ingoing bills");
        DocumentListParams.DocumentListParamsBuilder builder =DocumentListParams.builder();
        repository.findFirstByBillTypeOrderByBillDateDesc(BillType.INGOING_BILL).ifPresentOrElse((b) -> {
            builder.issuedFrom(b.getBillDate().plusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME));
        }, () -> {
            builder.issuedFrom(LocalDate.now().withDayOfMonth(1).minusWeeks(syncLookbackWeeks).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        });
        List<DocumentStatusResponse> documentResponses = eposlovanjeClient.getIncomingDocuments(builder.build());
        List<BillEntity> billEntities = documentResponses.stream()
                .map(dsr -> {
                    BillEntity billEntity = billEntityMapper.toIngoingBillEntity(dsr, BillType.INGOING_BILL);
                    billEntity.setPaymentReference(fetchPaymentReference(dsr.id()));
                    return billEntity;
                })
                .toList();
        logDuplicateSystemIds(billEntities);
        repository.saveAll(billEntities);
        log.info("Synced {} ingoing bill(s)", billEntities.size());
    }

    private String fetchPaymentReference(Long id) {
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(id);
        UblInvoice invoice = ublXmlService.parse(documentResponse.document());
        String paymentId = invoice.getPaymentMeans() != null ? invoice.getPaymentMeans().getPaymentId() : null;
        return paymentId == null ? null : HrPaymentReferenceService.fullReference(paymentId);
    }

    private void logDuplicateSystemIds(List<BillEntity> billEntities) {
        billEntities.stream()
                .collect(Collectors.groupingBy(BillEntity::getSystemId))
                .forEach((systemId, duplicates) -> {
                    if (duplicates.size() > 1) {
                        log.warn("Duplicate systemId {} found in {} bills: {}", systemId, duplicates.size(),
                                duplicates.stream().map(BillEntity::getFullBillId).toList());
                    }
                });
    }

    @Override
    public void deleteAll() {
        log.debug("Deleting all ingoing bills");
        repository.deleteAllByBillType(BillType.INGOING_BILL);
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for ingoing bill {}", id);
        BillEntity billEntity = repository.findBySystemIdAndBillType(Long.parseLong(id), BillType.INGOING_BILL)
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        billEntity.setSentCount(billEntity.getSentCount() + 1);
        repository.save(billEntity);
    }

    @Override
    public int markPaidFromBankStatement(CamtDocument statement) {
        List<BankTransactionEntity> transactions = camtStatementMapper.toBankTransactionEntities(statement, null);
        List<BillEntity> candidates = repository.findAllByBillTypeOrderByBillDateDesc(BillType.INGOING_BILL);
        int updated = 0;
        for (BankTransactionEntity tx : transactions) {
            if (tx.getCreditDebitIndicator() != CreditDebitIndicator.DBIT) continue;
            if (!supplierProperties.iban().equalsIgnoreCase(tx.getSenderIban())) continue;
            for (BillEntity bill : candidates) {
                if (HrPaymentReferenceService.matches(tx.getReference(), expectedReference(bill))) {
                    eposlovanjeClient.changeDocumentStatus(bill.getSystemId(), DocumentChangeStatusRequest.builder()
                                    .status(DocumentStatus.PlacenUPotpunosti.getValue())
                                    .changedOn(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")))
                            .build());
                    bill.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti);
                    repository.save(bill);
                    updated++;
                    break;
                }
            }
        }
        log.info("Marked {} ingoing bill(s) as paid from bank statement", updated);
        return updated;
    }

    private String expectedReference(BillEntity bill) {
        return bill.getPaymentReference() != null
                ? bill.getPaymentReference()
                : HrPaymentReferenceService.buildReference(bill.getFullBillId());
    }
}