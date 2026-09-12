package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentListParams;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BillInfoRepository;
import hr.bill.spring_bill.dao.BillItemRepository;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.bill_pdf.common.InvoiceLineDto;
import hr.bill.spring_bill.dto.bill_pdf.common.MonetaryTotalDto;
import hr.bill.spring_bill.dto.bill_pdf.common.TaxSubtotalDto;
import hr.bill.spring_bill.dto.bill_pdf.request.BillRequest;
import hr.bill.spring_bill.dto.bill_pdf.request.BillWithDetailsRequest;
import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request.EReportingReportDocumentRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentGetResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.PaymentParty;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.ApiResponse;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.BillSearchParams;
import hr.bill.spring_bill.dto.web.bill.b2b.ReportBillRequest;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.*;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.BillInfoEntity;
import hr.bill.spring_bill.model.BillItemEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.CroatianTimeZone;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.service.NumberToWordsService;
import hr.bill.spring_bill.service.UblXmlService;
import hr.bill.spring_bill.xml.ubl.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class F2ReportStrategy implements BillStrategy {

    private static final String CUSTOMIZATION_ID =
            "urn:cen.eu:en16931:2017#compliant#urn:mfin.gov.hr:cius-2025:1.0#conformant#urn:mfin.gov.hr:ext-2025:1.0";

    private final EposlovanjeClient eposlovanjeClient;

    private final EposlovanjeUtilClient eposlovanjeUtilClient;

    private final BillPdfClient billPdfClient;

    private final UblXmlService ublXmlService;

    private final SupplierProperties supplierProperties;

    private final PaymentInfoMapper paymentInfoMapper;

    private final BillEntityMapper billEntityMapper;

    private final BillInfoEntityMapper billInfoEntityMapper;

    private final BillInfoMapper billInfoMapper;

    private final PaymentReferenceMatcher paymentReferenceMatcher;

    private final BillRepository repository;

    private final BillInfoRepository billInfoRepository;

    private final BillItemRepository billItemRepository;

    @Value("${bill.eposlovanje.post-send-delay-ms:2000}")
    private long postSendDelayMs;

    @Override
    public BillReportType getType() {
        return BillReportType.F2_REPORT;
    }

    @Override
    public List<BillResponse> getBills() {
        List<BillEntity> bills = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F2_REPORT);
        return billEntityMapper.toBillResponseList(bills);
    }

    @Override
    public List<BillResponse> getBillsFilter(BillSearchParams params) {
        // F2_REPORT has no upstream source - sync() is a no-op for it and it's excluded from the
        // yearly deleteAllExceptReports() wipe, so the local table is the only place it ever lives.
        LocalDateTime from = params.dateFrom() == null ? null : params.dateFrom().atStartOfDay();
        LocalDateTime to = params.dateTill() == null ? null : params.dateTill().plusDays(1).atStartOfDay();
        List<BillEntity> bills = repository.findAllByBillTypeAndBillDateBetweenOptional(BillType.F2_REPORT, from, to);
        return paymentReferenceMatcher.markPaidByBillSystemId(billEntityMapper.toBillResponseList(bills));
    }

    @Override
    public PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart) {
        log.debug("Computing F2 report monthly totals for {}", monthStart);
        // F2_REPORT has no upstream source - sync() is a no-op for it and it's excluded from the
        // yearly deleteAllExceptReports() wipe, so the local table is the only place it ever lives.
        List<BillEntity> bills = repository.findAllByBillTypeAndBillDateBetween(
                BillType.F2_REPORT.name(), monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay());
        Set<String> paidReferences = paymentReferenceMatcher.paidReferences(CreditDebitIndicator.CRDT, supplierProperties.iban());
        Set<String> paidBillSystemIds = paymentReferenceMatcher.paidBillSystemIds(bills.stream()
                .map(BillEntity::getSystemId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList());
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal unpaid = BigDecimal.ZERO;
        for (BillEntity bill : bills) {
            if ((bill.getSystemId() != null && paidBillSystemIds.contains(String.valueOf(bill.getSystemId())))
                    || paymentReferenceMatcher.isPaid(paidReferences, bill.getFullBillId())) {
                paid = paid.add(bill.getTotalAmount());
            } else {
                unpaid = unpaid.add(bill.getTotalAmount());
            }
        }
        return new PaidUnpaidTotals(paid, unpaid);
    }

    @Override
    public Optional<LocalDate> findEarliestBillMonth() {
        return repository.findFirstByBillTypeOrderByBillDateAsc(BillType.F2_REPORT)
                .map(b -> b.getBillDate().toLocalDate().withDayOfMonth(1));
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for F2 report bill {}", id);
        UUID billId = UUID.fromString(id);
        BillEntity billEntity = repository.findById(billId)
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        BillInfoEntity billInfoEntity = billInfoRepository.findByBillId(billId)
                .orElseThrow(() -> new NotFoundException("Bill info not found for id: " + id));
        List<BillItemEntity> items = billItemRepository.findAllByBillInfoIdOrderByItemOrder(billInfoEntity.getId());
        PaymentParty buyerParty = PaymentParty.builder()
                .name(billInfoEntity.getBuyerName())
                .address(billInfoEntity.getBuyerAddress())
                .city(billInfoEntity.getBuyerCity())
                .postalZone(billInfoEntity.getBuyerPostalCode())
                .build();
        ApiResponse apiResponse = eposlovanjeUtilClient.generatePdf417(paymentInfoMapper.toPaymentInfo(
                supplierProperties,
                buyerParty,
                billInfoEntity.getCurrency(),
                billInfoEntity.getVatInclusiveAmount().doubleValue(),
                billInfoEntity.getPaymentModel(),
                billInfoEntity.getPaymentReference(),
                String.format("račun %s", billEntity.getFullBillId())
        ));
        BillWithDetailsRequest billWithDetailsRequest = billInfoEntityMapper.toBillWithDetailsRequest(
                billEntity, billInfoEntity, items, apiResponse.message());
        return BillDocument.builder()
                .content(billPdfClient.renderBillWithDetails(billWithDetailsRequest))
                .filename(billEntity.getFullBillId().replace("/", "-").replace("\\", "-"))
                .build();
    }

    @Override
    public BillResponse createBill(BaseBillRequest request) {
        if (request instanceof ReportBillRequest reportBillRequest) {
            log.info("Creating F2 report bill {} for buyer OIB {}", reportBillRequest.getBillId(), reportBillRequest.getBuyerOib());
            ComputedAmounts computed = computeAmounts(reportBillRequest, Boolean.FALSE);
            UblInvoice invoice = toUblInvoice(reportBillRequest, computed);
            String xmlRequest = ublXmlService.generateXml(invoice);
            EReportingReportDocumentRequest reportDocumentRequest = EReportingReportDocumentRequest.builder()
                    .document(xmlRequest)
                    .type("IR")
                    .build();
            eposlovanjeClient.reportDocument(reportDocumentRequest);
            BillEntity billEntity = billEntityMapper.toBillEntity(
                    reportBillRequest,
                    new BigDecimal(computed.monetaryTotal().taxInclusiveAmount()),
                    BillType.F2_REPORT);
            billEntity.setPaymentReference(HrPaymentReferenceService.fullReference(invoice.getPaymentMeans().getPaymentId()));
            billEntity = repository.save(billEntity);
            BillInfoEntity billInfoEntity = billInfoRepository.save(
                    billInfoEntityMapper.toBillInfoEntity(invoice, billEntity.getId()));
            billItemRepository.saveAll(
                    billInfoEntityMapper.toBillItemEntities(invoice.getInvoiceLines(), billInfoEntity.getId()));
            log.info("Created F2 report bill {}", billEntity.getFullBillId());
            return billEntityMapper.toBillResponse(billEntity);
        }
        return null;
    }

    @Override
    public BillInfoResponse getBillInfo(String id) {
        log.debug("Fetching bill info for F2 report bill {}", id);
        UUID billId = UUID.fromString(id);
        BillInfoEntity billInfoEntity = billInfoRepository.findByBillId(billId)
                .orElseThrow(() -> new NotFoundException("Bill info not found for id: " + id));
        List<BillItemEntity> items = billItemRepository.findAllByBillInfoIdOrderByItemOrder(billInfoEntity.getId());
        return billInfoMapper.toBillInfoResponse(billInfoEntity, items);
    }

    @Override
    public BillResponse cancel(String originalId, String newId) {
        log.info("Cancelling F2 report bill {} with replacement {}", originalId, newId);
        long parsedOriginalId = Long.parseLong(originalId);
        int parsedNewId = Integer.parseInt(newId);
        DocumentGetResponse response = eposlovanjeClient.getDocument(parsedOriginalId);
        UblInvoice orig = ublXmlService.parse(response.document());
        String xml = ublXmlService.generateXml(toCancellationUblInvoice(orig, parsedNewId));
        EReportingReportDocumentRequest reportDocumentRequest = EReportingReportDocumentRequest.builder()
                .document(xml)
                .type("IR")
                .build();
        eposlovanjeClient.reportDocument(reportDocumentRequest);
        try {
            Thread.sleep(postSendDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        List<DocumentStatusResponse> bills = eposlovanjeClient.getOutgoingDocuments(DocumentListParams.builder()
                .issuedFrom(LocalDate.now(CroatianTimeZone.ZONE).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .issuedTo(LocalDate.now(CroatianTimeZone.ZONE).plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .build());
        DocumentStatusResponse lastBill = bills.stream()
                .filter(b -> b.documentId().equals(String.format("%d/1/1", parsedNewId)))
                .findAny().get();
        BillEntity billEntity = repository.save(billEntityMapper.toBillEntity(lastBill, BillType.F2_REPORT));
        return billEntityMapper.toBillResponse(billEntity);
    }

    @Override
    public BillReviewResponse reviewBill(BaseBillRequest request) {
        if (request instanceof ReportBillRequest reportBillRequest) {
            log.debug("Reviewing F2 report bill request for buyer OIB {}", reportBillRequest.getBuyerOib());
            ComputedAmounts computedAmounts = computeAmounts(reportBillRequest, Boolean.FALSE);
            return BillReviewResponse.builder()
                    .billNumber(String.format("%d/1/1", reportBillRequest.getBillId()))
                    .billDate(reportBillRequest.getBillDate())
                    .billTime(reportBillRequest.getBillTime())
                    .dueDate(reportBillRequest.getDueDate())
                    .name(reportBillRequest.getBillItemName())
                    .description(reportBillRequest.getBillItemDescription())
                    .pdvType(reportBillRequest.getVatCategory())
                    .profile(reportBillRequest.getProfile())
                    .orderNumber(reportBillRequest.getOrderNumber())
                    .note(reportBillRequest.getNote())
                    .buyerOib(reportBillRequest.getBuyerOib())
                    .buyerName(reportBillRequest.getBuyerName())
                    .buyerAddress(reportBillRequest.getBuyerStreet())
                    .buyerCity(reportBillRequest.getBuyerCity())
                    .buyerPostalZone(reportBillRequest.getBuyerPostalZone())
                    .baseAmount(new BigDecimal(computedAmounts.monetaryTotal().taxExclusiveAmount()))
                    .taxAmount(new BigDecimal(computedAmounts.taxTotalAmount()))
                    .totalAmount(new BigDecimal(computedAmounts.monetaryTotal().taxInclusiveAmount()))
                    .build();
        }
        return null;
    }

    @Override
    public void sync() {

    }

    @Override
    public void deleteAll() {
        log.debug("Deleting all F2 report bills");
        repository.deleteAllByBillType(BillType.F2_REPORT);
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for F2 report bill {}", id);
        BillEntity billEntity = repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        billEntity.setSentCount(billEntity.getSentCount() + 1);
        repository.save(billEntity);
    }

    @Override
    public int markPaidFromBankStatement(List<BankTransactionEntity> transactions) {
        List<BillEntity> candidates = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F2_REPORT);
        int updated = 0;
        for (BankTransactionEntity tx : transactions) {
            if (tx.getCreditDebitIndicator() != CreditDebitIndicator.CRDT) continue;
            if (!supplierProperties.iban().equalsIgnoreCase(tx.getReceiverIban())) continue;
            for (BillEntity bill : candidates) {
                if (HrPaymentReferenceService.matches(tx.getReference(), expectedReference(bill))) {
                    bill.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti);
                    repository.save(bill);
                    tx.setBillSystemId(billSystemId(bill));
                    updated++;
                    break;
                }
            }
        }
        log.info("Marked {} F2 report bill(s) as paid from bank statement", updated);
        return updated;
    }

    private String expectedReference(BillEntity bill) {
        return bill.getPaymentReference() != null
                ? bill.getPaymentReference()
                : HrPaymentReferenceService.buildReference(bill.getFullBillId());
    }

    private ComputedAmounts computeAmounts(ReportBillRequest request, Boolean negative) {
        int quantity = negative ? -1 : 1;
        VatCategory vatCat = request.getVatCategory();
        BigDecimal lineExt = request.getBaseAmount().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        // BT-146 (Item net price) must never be negative per EN16931 BR-27. For a storno the
        // sign is carried by the invoiced quantity (-1) and the line extension amount, not the price.
        BigDecimal unitPrice = request.getBaseAmount().abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = lineExt.multiply(vatCat.getRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxInclusive = lineExt.add(taxAmount);

        TaxSubtotalDto subtotal = new TaxSubtotalDto(
                vatCat.getId(),
                fmtRate(vatCat.getRate()),
                lineExt.toPlainString(),
                taxAmount.toPlainString(),
                vatCat.getTaxExemptionReason()
        );

        InvoiceLineDto line = new InvoiceLineDto(
                1,
                request.getBillItemName(),
                request.getBillItemDescription(),
                "71.12.20",
                String.valueOf(quantity),
                "H87",
                unitPrice.toPlainString(),
                lineExt.toPlainString(),
                vatCat.getId()
        );

        MonetaryTotalDto monetaryTotal = new MonetaryTotalDto(
                lineExt.toPlainString(),
                lineExt.toPlainString(),
                taxInclusive.toPlainString(),
                "0.00",
                taxInclusive.toPlainString()
        );

        return new ComputedAmounts(
                "EUR",
                String.format("HR00 %d-1-1", request.getBillId()),
                taxAmount.toPlainString(),
                List.of(subtotal),
                List.of(line),
                monetaryTotal
        );
    }

    private record ComputedAmounts(
            String cur,
            String paymentId,
            String taxTotalAmount,
            List<TaxSubtotalDto> taxSubtotals,
            List<InvoiceLineDto> lines,
            MonetaryTotalDto monetaryTotal
    ) {}

    private UblInvoice toUblInvoice(ReportBillRequest request, ComputedAmounts computed) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return UblInvoice.builder()
                .ublExtensions(UblExtensions.empty())
                .customizationId(CUSTOMIZATION_ID)
                .profileId(request.getProfile().name())
                .id(String.format("%d/1/1", request.getBillId()))
                .copyIndicator(Boolean.FALSE)
                .issueDate(request.getBillDate().format(dateFormat))
                .issueTime(request.getBillTime().format(timeFormat))
                .dueDate(request.getDueDate().format(dateFormat))
                .invoiceTypeCode("380")
                .notes(nullIfBlank(request.getNote()) != null ? List.of(request.getNote()) : null)
                .documentCurrencyCode(computed.cur())
                .invoicePeriod(UblInvoicePeriod.builder()
                        .startDate(request.getBillDate().withDayOfMonth(1).format(dateFormat))
                        .endDate(request.getBillDate().format(dateFormat))
                        .build())
                .orderReference(nullIfBlank(request.getOrderNumber()) != null
                        ? UblOrderReference.builder().id(request.getOrderNumber()).build()
                        : null)
                .accountingSupplierParty(buildSupplierParty())
                .accountingCustomerParty(UblAccountingCustomerParty.builder()
                        .party(UblParty.builder()
                                .endpointId(UblEndpointId.builder().schemeId("9934").value(request.getBuyerOib()).build())
                                .partyName(UblPartyName.builder().name(request.getBuyerName()).build())
                                .postalAddress(UblPostalAddress.builder()
                                        .streetName(request.getBuyerStreet())
                                        .cityName(request.getBuyerCity())
                                        .postalZone(request.getBuyerPostalZone())
                                        .country(UblPostalAddress.Country.builder()
                                                .identificationCode(supplierProperties.countryCode())
                                                .build())
                                        .build())
                                .partyTaxScheme(UblPartyTaxScheme.builder()
                                        .companyId("HR" + request.getBuyerOib())
                                        .taxScheme(UblTaxScheme.vat())
                                        .build())
                                .partyLegalEntity(UblPartyLegalEntity.builder()
                                        .registrationName(request.getBuyerName())
                                        .companyId(request.getBuyerOib())
                                        .build())
                                .build())
                        .build())
                .paymentMeans(UblPaymentMeans.builder()
                        .paymentMeansCode(PaymentMeans.CreditTransfer.getCode())
                        .paymentDueDate(request.getDueDate().format(dateFormat))
                        .paymentChannelCode("IBAN")
                        .instructionNote(String.format("račun %d/1/1", request.getBillId()))
                        .paymentId(computed.paymentId())
                        .payeeFinancialAccount(UblPaymentMeans.Account.builder()
                                .id(supplierProperties.iban())
                                .currencyCode(computed.cur())
                                .build())
                        .build())
                .taxTotal(UblTaxTotal.builder()
                        .taxAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.taxTotalAmount()).build())
                        .taxSubtotals(computed.taxSubtotals().stream()
                                .map(sub -> UblTaxSubtotal.builder()
                                        .taxableAmount(UblAmount.builder().currencyId(computed.cur()).value(sub.taxableAmount()).build())
                                        .taxAmount(UblAmount.builder().currencyId(computed.cur()).value(sub.taxAmount()).build())
                                        .taxCategory(UblTaxCategory.builder()
                                                .id(sub.categoryId())
                                                .percent(sub.percent())
                                                .taxExemptionReason(sub.taxExemptionReason())
                                                .taxScheme(UblTaxScheme.vat())
                                                .build())
                                        .build())
                                .toList())
                        .build())
                .legalMonetaryTotal(UblLegalMonetaryTotal.builder()
                        .lineExtensionAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.monetaryTotal().lineExtensionAmount()).build())
                        .taxExclusiveAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.monetaryTotal().taxExclusiveAmount()).build())
                        .taxInclusiveAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.monetaryTotal().taxInclusiveAmount()).build())
                        .prepaidAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.monetaryTotal().prepaidAmount()).build())
                        .payableAmount(UblAmount.builder().currencyId(computed.cur()).value(computed.monetaryTotal().payableAmount()).build())
                        .build())
                .invoiceLines(computed.lines().stream()
                        .map(line -> {
                            VatCategory vatCat = VatCategory.fromId(line.vatCategory());
                            String vatName = vatCat != null ? vatCat.getVatName() : "";
                            String vatPercent = vatCat != null ? fmtRate(vatCat.getRate()) : "0.0";
                            return UblInvoiceLine.builder()
                                    .id(String.valueOf(line.id()))
                                    .invoicedQuantity(UblQuantity.builder()
                                            .unitCode(line.unitCode())
                                            .value(line.quantity())
                                            .build())
                                    .lineExtensionAmount(UblAmount.builder()
                                            .currencyId(computed.cur())
                                            .value(line.lineExtensionAmount())
                                            .build())
                                    .item(UblItem.builder()
                                            .description(line.description())
                                            .name(line.name())
                                            .commodityClassification(UblCommodityClassification.builder()
                                                    .itemClassificationCode(UblClassificationCode.builder()
                                                            .listId("CG")
                                                            .value(line.classificationCode())
                                                            .build())
                                                    .build())
                                            .classifiedTaxCategory(UblClassifiedTaxCategory.builder()
                                                    .id(line.vatCategory())
                                                    .name(vatName)
                                                    .percent(vatPercent)
                                                    .taxScheme(UblTaxScheme.vat())
                                                    .build())
                                            .build())
                                    .price(UblPrice.builder()
                                            .priceAmount(UblAmount.builder()
                                                    .currencyId(computed.cur())
                                                    .value(line.unitPrice())
                                                    .build())
                                            .baseQuantity(UblQuantity.builder()
                                                    .unitCode(line.unitCode())
                                                    .value("1")
                                                    .build())
                                            .build())
                                    .build();
                        })
                        .toList())
                .additionalDocumentReferences(buildAdditionalDocumentReferences(request, computed))
                .build();
    }

    private UblInvoice toCancellationUblInvoice(UblInvoice orig, int newBillId) {
        String cancelNote = "storno rčn. " + orig.getId() + ".";
        String cur = orig.getDocumentCurrencyCode() != null ? orig.getDocumentCurrencyCode() : "EUR";
        UblPaymentMeans origPm = orig.getPaymentMeans();
        ComputedAmounts computed = computeAmounts(ReportBillRequest.builder()
                .billItemName(orig.getInvoiceLines().getFirst().getItem().getName())
                .billItemDescription(orig.getInvoiceLines().getFirst().getItem().getDescription())
                .baseAmount(new BigDecimal(orig.getLegalMonetaryTotal().getTaxExclusiveAmount().getValue()))
                .vatCategory(VatCategory.fromId(orig.getTaxTotal().getTaxSubtotals().getFirst().getTaxCategory().getId()))
                .build(), Boolean.TRUE);

        return UblInvoice.builder()
                .ublExtensions(UblExtensions.empty())
                .customizationId(CUSTOMIZATION_ID)
                .profileId("P10")
                .id(String.format("%d/1/1", newBillId))
                .copyIndicator(Boolean.FALSE)
                .issueDate(orig.getIssueDate())
                .issueTime(orig.getIssueTime())
                .dueDate(orig.getDueDate())
                .invoiceTypeCode("384")
                .notes(orig.getNotes())
                .documentCurrencyCode(cur)
                .invoicePeriod(orig.getInvoicePeriod())
                .orderReference(orig.getOrderReference())
                .accountingSupplierParty(buildSupplierParty())
                .accountingCustomerParty(orig.getAccountingCustomerParty())
                .paymentMeans(origPm == null ? null : UblPaymentMeans.builder()
                        .paymentMeansCode(origPm.getPaymentMeansCode())
                        .paymentDueDate(origPm.getPaymentDueDate())
                        .paymentChannelCode(origPm.getPaymentChannelCode())
                        .instructionNote(cancelNote)
                        .paymentId(computed.paymentId())
                        .payeeFinancialAccount(origPm.getPayeeFinancialAccount())
                        .build())
                .taxTotal(UblTaxTotal.builder()
                        .taxAmount(UblAmount.builder().currencyId(cur).value(computed.taxTotalAmount()).build())
                        .taxSubtotals(computed.taxSubtotals().stream()
                                .map(sub -> UblTaxSubtotal.builder()
                                        .taxableAmount(UblAmount.builder().currencyId(cur).value(sub.taxableAmount()).build())
                                        .taxAmount(UblAmount.builder().currencyId(cur).value(sub.taxAmount()).build())
                                        .taxCategory(UblTaxCategory.builder()
                                                .id(sub.categoryId())
                                                .percent(sub.percent())
                                                .taxExemptionReason(sub.taxExemptionReason())
                                                .taxScheme(UblTaxScheme.vat())
                                                .build())
                                        .build())
                                .toList())
                        .build())
                .legalMonetaryTotal(UblLegalMonetaryTotal.builder()
                        .lineExtensionAmount(UblAmount.builder().currencyId(cur).value(computed.monetaryTotal().lineExtensionAmount()).build())
                        .taxExclusiveAmount(UblAmount.builder().currencyId(cur).value(computed.monetaryTotal().taxExclusiveAmount()).build())
                        .taxInclusiveAmount(UblAmount.builder().currencyId(cur).value(computed.monetaryTotal().taxInclusiveAmount()).build())
                        .prepaidAmount(UblAmount.builder().currencyId(cur).value(computed.monetaryTotal().prepaidAmount()).build())
                        .payableAmount(UblAmount.builder().currencyId(cur).value(computed.monetaryTotal().payableAmount()).build())
                        .build())
                .invoiceLines(computed.lines().stream()
                        .map(line -> {
                            VatCategory vatCat = VatCategory.fromId(line.vatCategory());
                            String vatName = vatCat != null ? vatCat.getVatName() : "";
                            String vatPercent = vatCat != null ? fmtRate(vatCat.getRate()) : "0.0";
                            return UblInvoiceLine.builder()
                                    .id(String.valueOf(line.id()))
                                    .invoicedQuantity(UblQuantity.builder()
                                            .unitCode(line.unitCode())
                                            .value(line.quantity())
                                            .build())
                                    .lineExtensionAmount(UblAmount.builder()
                                            .currencyId(cur)
                                            .value(line.lineExtensionAmount())
                                            .build())
                                    .item(UblItem.builder()
                                            .description(line.description())
                                            .name(line.name())
                                            .commodityClassification(UblCommodityClassification.builder()
                                                    .itemClassificationCode(UblClassificationCode.builder()
                                                            .listId("CG")
                                                            .value(line.classificationCode())
                                                            .build())
                                                    .build())
                                            .classifiedTaxCategory(UblClassifiedTaxCategory.builder()
                                                    .id(line.vatCategory())
                                                    .name(vatName)
                                                    .percent(vatPercent)
                                                    .taxScheme(UblTaxScheme.vat())
                                                    .build())
                                            .build())
                                    .price(UblPrice.builder()
                                            .priceAmount(UblAmount.builder()
                                                    .currencyId(cur)
                                                    .value(line.unitPrice())
                                                    .build())
                                            .baseQuantity(UblQuantity.builder()
                                                    .unitCode(line.unitCode())
                                                    .value("1")
                                                    .build())
                                            .build())
                                    .build();
                        })
                        .toList())
                .build();
    }

    private UblAccountingSupplierParty buildSupplierParty() {
        return UblAccountingSupplierParty.builder()
                .party(UblParty.builder()
                        .endpointId(UblEndpointId.builder().schemeId("9934").value(supplierProperties.oib()).build())
                        .partyName(UblPartyName.builder().name(supplierProperties.name()).build())
                        .postalAddress(UblPostalAddress.builder()
                                .streetName(supplierProperties.street())
                                .cityName(supplierProperties.city())
                                .postalZone(supplierProperties.postalZone())
                                .country(UblPostalAddress.Country.builder()
                                        .identificationCode(supplierProperties.countryCode())
                                        .build())
                                .build())
                        .partyTaxScheme(UblPartyTaxScheme.builder()
                                .companyId("HR" + supplierProperties.oib())
                                .taxScheme(UblTaxScheme.vat())
                                .build())
                        .partyLegalEntity(UblPartyLegalEntity.builder()
                                .registrationName(supplierProperties.name())
                                .companyId(supplierProperties.oib())
                                .companyLegalForm(supplierProperties.name())
                                .build())
                        .contact(UblContact.builder()
                                .name(supplierProperties.contactName())
                                .telephone(supplierProperties.phone())
                                .electronicMail(supplierProperties.email())
                                .build())
                        .build())
                .sellerContact(UblSellerContact.builder()
                        .id(supplierProperties.contactOib())
                        .name(supplierProperties.contactName())
                        .build())
                .build();
    }

    private UblAdditionalDocumentReference.Attachment.EmbeddedDocumentBinaryObject createXmlReport(ReportBillRequest request, ComputedAmounts computedAmounts) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        PaymentParty buyerParty = PaymentParty.builder()
                .name(request.getBuyerName())
                .address(request.getBuyerStreet())
                .city(request.getBuyerCity())
                .postalZone(request.getBuyerPostalZone())
                .build();
        ApiResponse apiResponse = eposlovanjeUtilClient.generatePdf417(paymentInfoMapper.toPaymentInfo(
                supplierProperties,
                buyerParty,
                computedAmounts.cur(),
                new BigDecimal(computedAmounts.monetaryTotal().taxInclusiveAmount()).doubleValue(),
                HrPaymentReferenceService.extractHrModel(computedAmounts.paymentId()),
                HrPaymentReferenceService.trimHrPrefix(computedAmounts.paymentId()),
                String.format("račun %d/1/1", request.getBillId())
        ));
        BillRequest pdfBillRequest = BillRequest.builder()
                .billNumber(String.format("%d/1/1", request.getBillId()))
                .billDate(request.getBillDate().format(dateFormatter))
                .billTime(request.getBillTime().format(timeFormatter))
                .billBasePrice(computedAmounts.monetaryTotal().taxExclusiveAmount().replace('.', ','))
                .billPdvPrice(computedAmounts.taxTotalAmount().replace('.', ','))
                .billTotalPrice(computedAmounts.monetaryTotal().taxInclusiveAmount().replace('.', ','))
                .billProjectName(request.getBillItemName())
                .billProjectDescription(request.getBillItemDescription())
                .billTotalText(NumberToWordsService.asWords(new BigDecimal(computedAmounts.monetaryTotal().taxInclusiveAmount())))
                .paymentDays(ChronoUnit.DAYS.between(request.getDueDate(), request.getBillDate()))
                .recipientName(request.getBuyerName())
                .recipientOib(request.getBuyerOib())
                .recipientAddress(request.getBuyerStreet())
                .recipientCity(request.getBuyerCity())
                .recipientPost(request.getBuyerPostalZone())
                .billReverseCharge(request.getVatCategory().equals(VatCategory.ReverseCharge))
                .pdf417Image(apiResponse.message())
                .build();
        byte[] pdfBytes = billPdfClient.renderBill(pdfBillRequest);
        String filename = pdfBillRequest.billNumber().replace('/', '-').replace('\\', '-') + ".pdf";
        return UblAdditionalDocumentReference.Attachment.EmbeddedDocumentBinaryObject.builder()
                .mimeCode("application/pdf")
                .filename(filename)
                .value(Base64.getEncoder().encodeToString(pdfBytes))
                .build();
    }

    private List<UblAdditionalDocumentReference> buildAdditionalDocumentReferences(ReportBillRequest request, ComputedAmounts computed) {
        List<UblAdditionalDocumentReference> references = new ArrayList<>();
        references.add(UblAdditionalDocumentReference.builder()
                .id("1")
                .attachment(UblAdditionalDocumentReference.Attachment.builder()
                        .embeddedDocumentBinaryObject(createXmlReport(request, computed))
                        .build())
                .build());
        if (request.getOrderDocumentBytes() != null) {
            references.add(UblAdditionalDocumentReference.builder()
                    .id("2")
                    .attachment(UblAdditionalDocumentReference.Attachment.builder()
                            .embeddedDocumentBinaryObject(UblAdditionalDocumentReference.Attachment.EmbeddedDocumentBinaryObject.builder()
                                    .mimeCode("application/pdf")
                                    .filename(request.getOrderDocumentFilename())
                                    .value(Base64.getEncoder().encodeToString(request.getOrderDocumentBytes()))
                                    .build())
                            .build())
                    .build());
        }
        return references;
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String fmtRate(BigDecimal rate) {
        if (rate.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return String.format("%.1f", rate);
        }
        return rate.stripTrailingZeros().toPlainString();
    }
}