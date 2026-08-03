package hr.bill.spring_bill.service.document;

import feign.FeignException;
import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.eposlovanje.EposlovanjeClient;
import hr.bill.spring_bill.clients.eposlovanje.params.DocumentListParams;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.bill_pdf.common.InvoiceLineDto;
import hr.bill.spring_bill.dto.bill_pdf.common.MonetaryTotalDto;
import hr.bill.spring_bill.dto.bill_pdf.common.TaxSubtotalDto;
import hr.bill.spring_bill.dto.bill_pdf.request.BillRequest;
import hr.bill.spring_bill.dto.bill_pdf.request.BillWithDetailsRequest;
import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.request.DocumentSendRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentGetResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response.DocumentStatusResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.PaymentParty;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.ApiResponse;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.BusinessEntity;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.BusinessCheckResponse;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.b2b.F2BillRequest;
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
import hr.bill.spring_bill.service.BusinessEntityService;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.service.NumberToWordsService;
import hr.bill.spring_bill.service.UblXmlService;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class F2OutgoingStrategy implements BillStrategy {

    private static final String CUSTOMIZATION_ID =
            "urn:cen.eu:en16931:2017#compliant#urn:mfin.gov.hr:cius-2025:1.0#conformant#urn:mfin.gov.hr:ext-2025:1.0";

    private final EposlovanjeClient eposlovanjeClient;

    private final EposlovanjeUtilClient eposlovanjeUtilClient;

    private final BillPdfClient billPdfClient;

    private final UblXmlService ublXmlService;
    
    private final BusinessEntityService businessEntityService;

    private final SupplierProperties supplierProperties;

    @Value("${bill.schedule.sync-lookback-weeks}")
    private long syncLookbackWeeks;

    private final PaymentInfoMapper paymentInfoMapper;

    private final UblInvoiceMapper ublInvoiceMapper;

    private final BillEntityMapper billEntityMapper;

    private final BillInfoMapper billInfoMapper;

    private final CamtStatementMapper camtStatementMapper;

    private final PaymentReferenceMatcher paymentReferenceMatcher;

    private final BillRepository repository;

    @Override
    public BillReportType getType() {
        return BillReportType.F2_OUTGOING;
    }

    @Override
    public List<BillResponse> getBills() {
        List<BillEntity> bills = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F2_BILL);
        return billEntityMapper.toBillResponseList(bills);
    }

    @Override
    public PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart) {
        log.debug("Computing F2 outgoing monthly totals for {}", monthStart);
        DocumentListParams params = DocumentListParams.builder()
                .issuedFrom(monthStart.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .issuedTo(monthStart.plusMonths(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
        Set<String> paidReferences = paymentReferenceMatcher.paidReferences(CreditDebitIndicator.CRDT, supplierProperties.iban());
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal unpaid = BigDecimal.ZERO;
        for (DocumentStatusResponse d : eposlovanjeClient.getOutgoingDocuments(params)) {
            BigDecimal amount = BigDecimal.valueOf(d.amount());
            if (paymentReferenceMatcher.isPaid(paidReferences, d.documentId())) {
                paid = paid.add(amount);
            } else {
                unpaid = unpaid.add(amount);
            }
        }
        return new PaidUnpaidTotals(paid, unpaid);
    }

    @Override
    public Optional<LocalDate> findEarliestBillMonth() {
        return eposlovanjeClient.getOutgoingDocuments(DocumentListParams.builder().build()).stream()
                .map(d -> LocalDateTime.parse(d.issuedOn()))
                .min(LocalDateTime::compareTo)
                .map(dt -> dt.toLocalDate().withDayOfMonth(1));
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for F2 outgoing bill {}", id);
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(Long.parseLong(id));
        UblInvoice ublInvoice = ublXmlService.parse(documentResponse.document());
        ApiResponse apiResponse = eposlovanjeUtilClient.generatePdf417(paymentInfoMapper.toPaymentInfo(supplierProperties, ublInvoice));
        BillWithDetailsRequest billWithDetailsRequest = ublInvoiceMapper.toBillWithDetailsRequest(ublInvoice, apiResponse.message());
        return BillDocument.builder()
                .content(billPdfClient.renderBillWithDetails(billWithDetailsRequest))
                .filename(ublInvoice.getId().replace("/", "-").replace("\\", "-"))
                .build();
    }

    @Override
    public BillResponse createBill(BaseBillRequest request) {
        if (request instanceof F2BillRequest f2BillRequest) {
                log.info("Creating F2 outgoing bill {} for buyer OIB {}", f2BillRequest.getBillId(), f2BillRequest.getBuyerOib());
                BusinessCheckResponse businessCheckResponse = businessEntityService.checkByOib(f2BillRequest.getBuyerOib());
                if(!businessCheckResponse.amsCheckResponse().publishedOnAms()) {
                    throw new IllegalArgumentException("Business entity failed AMS check");
                }
                UblInvoice invoice = toUblInvoice(f2BillRequest, Optional.ofNullable(businessCheckResponse.businessEntity()));
                String xmlRequest = ublXmlService.generateXml(invoice);
                DocumentSendRequest documentSendRequest = DocumentSendRequest.builder()
                        .document(xmlRequest)
                        .softwareId("TEHNOMODUS_SPRING_APP")
                        .sendAsEmail(false)
                        .build();
                try {
                    eposlovanjeClient.sendDocument(documentSendRequest);
                }
                catch (FeignException.FeignClientException e) {
                    throw e;
                }
                List<DocumentStatusResponse> bills = eposlovanjeClient.getOutgoingDocuments(DocumentListParams.builder()
                        .issuedFrom(LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                        .issuedTo(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build());
                DocumentStatusResponse lastBill = bills.stream()
                        .filter(b -> b.documentId().equals(String.format("%d/1/1", f2BillRequest.getBillId())))
                        .findAny().get();
                BillEntity billEntity = billEntityMapper.toBillEntity(lastBill, BillType.F2_BILL);
                billEntity.setPaymentReference(HrPaymentReferenceService.fullReference(invoice.getPaymentMeans().getPaymentId()));
                billEntity = repository.save(billEntity);
                log.info("Created F2 outgoing bill {}", billEntity.getFullBillId());
                return billEntityMapper.toBillResponse(billEntity);
        }
        return null;
    }

    @Override
    public BillInfoResponse getBillInfo(String id) {
        log.debug("Fetching bill info for F2 outgoing bill {}", id);
        DocumentGetResponse documentResponse = eposlovanjeClient.getDocument(Long.parseLong(id));
        UblInvoice ublInvoice = ublXmlService.parse(documentResponse.document());
        return billInfoMapper.toBillInfoResponse(ublInvoice);
    }

    @Override
    public BillResponse cancel(String originalId, String newId) {
        log.info("Cancelling F2 outgoing bill {} with replacement {}", originalId, newId);
        long parsedOriginalId = Long.parseLong(originalId);
        int parsedNewId = Integer.parseInt(newId);
        DocumentGetResponse response = eposlovanjeClient.getDocument(parsedOriginalId);
        UblInvoice orig = ublXmlService.parse(response.document());
        String xml = ublXmlService.generateXml(toCancellationUblInvoice(orig, parsedNewId));
        DocumentSendRequest documentSendRequest = DocumentSendRequest.builder()
                .document(xml)
                .softwareId("TEHNOMODUS_SPRING_APP")
                .sendAsEmail(false)
                .build();
        eposlovanjeClient.sendDocument(documentSendRequest);
        List<DocumentStatusResponse> bills = eposlovanjeClient.getOutgoingDocuments(DocumentListParams.builder()
                .issuedFrom(LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME))
                .issuedTo(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build());
        DocumentStatusResponse lastBill = bills.stream()
                .filter(b -> b.documentId().equals(String.format("%d/1/1", parsedNewId)))
                .findAny().get();
        BillEntity billEntity = repository.save(billEntityMapper.toBillEntity(lastBill, BillType.F2_BILL));
        return billEntityMapper.toBillResponse(billEntity);
    }

    @Override
    public BillReviewResponse reviewBill(BaseBillRequest request) {
        if (request instanceof F2BillRequest f2BillRequest) {
            log.debug("Reviewing F2 outgoing bill request for buyer OIB {}", f2BillRequest.getBuyerOib());
            BillReviewResponse.BillReviewResponseBuilder builder = BillReviewResponse.builder()
                    .billNumber(String.format("%d/1/1", f2BillRequest.getBillId()))
                    .billDate(f2BillRequest.getBillDate())
                    .billTime(f2BillRequest.getBillTime())
                    .dueDate(f2BillRequest.getDueDate())
                    .name(f2BillRequest.getBillItemName())
                    .description(f2BillRequest.getBillItemDescription())
                    .pdvType(f2BillRequest.getVatCategory())
                    .profile(f2BillRequest.getProfile())
                    .orderNumber(f2BillRequest.getOrderNumber())
                    .note(f2BillRequest.getNote());
            businessEntityService.findByOib(f2BillRequest.getBuyerOib()).ifPresentOrElse(
                    be -> builder.buyerOib(be.oib())
                            .buyerName(be.name())
                            .buyerAddress(be.headquatersAddress())
                            .buyerCity(be.headquatersCity())
                            .buyerPostalZone(be.headquatersZip()),
                    () -> builder.buyerOib(f2BillRequest.getBuyerOib())
                            .buyerName(f2BillRequest.getBuyerName())
                            .buyerAddress(f2BillRequest.getBuyerAddress())
                            .buyerCity(f2BillRequest.getBuyerCity())
                            .buyerPostalZone(f2BillRequest.getBuyerPostalCode())
            );
            ComputedAmounts computedAmounts = computeAmounts(f2BillRequest, Boolean.FALSE);
            builder.baseAmount(new BigDecimal(computedAmounts.monetaryTotal().taxExclusiveAmount()))
                    .taxAmount(new BigDecimal(computedAmounts.taxTotalAmount()))
                    .totalAmount(new BigDecimal(computedAmounts.monetaryTotal().taxInclusiveAmount()));
            return builder.build();
        }
        return null;
    }

    @Override
    public void sync() {
        log.debug("Syncing F2 outgoing bills");
        DocumentListParams.DocumentListParamsBuilder builder =DocumentListParams.builder();
        repository.findFirstByBillTypeOrderByBillDateDesc(BillType.F2_BILL).ifPresentOrElse((b) -> {
            builder.issuedFrom(b.getBillDate().plusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME));
        }, () -> {
            builder.issuedFrom(LocalDate.now().withDayOfMonth(1).minusWeeks(syncLookbackWeeks).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        });
        List<DocumentStatusResponse> documentResponses = eposlovanjeClient.getOutgoingDocuments(builder.build());
        List<BillEntity> billEntities = documentResponses.stream()
                .map(dsr -> {
                    BillEntity billEntity = billEntityMapper.toBillEntity(dsr, BillType.F2_BILL);
                    billEntity.setPaymentReference(fetchPaymentReference(dsr.id()));
                    return billEntity;
                })
                .toList();
        logDuplicateSystemIds(billEntities);
        repository.saveAll(billEntities);
        log.info("Synced {} F2 outgoing bill(s)", billEntities.size());
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
        log.debug("Deleting all F2 outgoing bills");
        repository.deleteAllByBillType(BillType.F2_BILL);
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for F2 outgoing bill {}", id);
        BillEntity billEntity = repository.findBySystemIdAndBillType(Long.parseLong(id), BillType.F2_BILL)
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        billEntity.setSentCount(billEntity.getSentCount() + 1);
        repository.save(billEntity);
    }

    @Override
    public int markPaidFromBankStatement(CamtDocument statement) {
        List<BankTransactionEntity> transactions = camtStatementMapper.toBankTransactionEntities(statement, null);
        List<BillEntity> candidates = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F2_BILL);
        int updated = 0;
        for (BankTransactionEntity tx : transactions) {
            if (tx.getCreditDebitIndicator() != CreditDebitIndicator.CRDT) continue;
            if (!supplierProperties.iban().equalsIgnoreCase(tx.getReceiverIban())) continue;
            for (BillEntity bill : candidates) {
                if (HrPaymentReferenceService.matches(tx.getReference(), expectedReference(bill))) {
                    bill.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti);
                    repository.save(bill);
                    updated++;
                    break;
                }
            }
        }
        log.info("Marked {} F2 outgoing bill(s) as paid from bank statement", updated);
        return updated;
    }

    private String expectedReference(BillEntity bill) {
        return bill.getPaymentReference() != null
                ? bill.getPaymentReference()
                : HrPaymentReferenceService.buildReference(bill.getFullBillId());
    }

    private ComputedAmounts computeAmounts(F2BillRequest request, Boolean negative) {
        int quantity = negative ? -1 : 1;
        VatCategory vatCat = request.getVatCategory();
        BigDecimal lineExt = request.getBaseAmount().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
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
                lineExt.toPlainString(),
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

    private UblInvoice toUblInvoice(F2BillRequest request, Optional<BusinessEntity> businessEntity) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        ComputedAmounts computed = computeAmounts(request, Boolean.FALSE);
        String buyerOib = businessEntity.map(BusinessEntity::oib).orElse(request.getBuyerOib());
        String buyerName = businessEntity.map(BusinessEntity::name).orElse(request.getBuyerName());
        String buyerStreet = businessEntity.map(BusinessEntity::headquatersAddress).orElse(request.getBuyerAddress());
        String buyerCity = businessEntity.map(BusinessEntity::headquatersCity).orElse(request.getBuyerCity());
        String buyerPostalZone = businessEntity.map(BusinessEntity::headquatersZip).orElse(request.getBuyerPostalCode());
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
                                .endpointId(UblEndpointId.builder().schemeId("9934").value(buyerOib).build())
                                .partyName(UblPartyName.builder().name(buyerName).build())
                                .postalAddress(UblPostalAddress.builder()
                                        .streetName(buyerStreet)
                                        .cityName(buyerCity)
                                        .postalZone(buyerPostalZone)
                                        .country(UblPostalAddress.Country.builder()
                                                .identificationCode(supplierProperties.countryCode())
                                                .build())
                                        .build())
                                .partyTaxScheme(UblPartyTaxScheme.builder()
                                        .companyId("HR" + buyerOib)
                                        .taxScheme(UblTaxScheme.vat())
                                        .build())
                                .partyLegalEntity(UblPartyLegalEntity.builder()
                                        .registrationName(buyerName)
                                        .companyId(buyerOib)
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
                .additionalDocumentReferences(buildAdditionalDocumentReferences(request, computed, businessEntity))
                .build();
    }

    private UblInvoice toCancellationUblInvoice(UblInvoice orig, int newBillId) {
        String cancelNote = "storno rčn. " + orig.getId() + ".";
        String cur = orig.getDocumentCurrencyCode() != null ? orig.getDocumentCurrencyCode() : "EUR";
        UblPaymentMeans origPm = orig.getPaymentMeans();
        ComputedAmounts computed = computeAmounts(F2BillRequest.builder()
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

    private UblAdditionalDocumentReference.Attachment.EmbeddedDocumentBinaryObject createXmlReport(F2BillRequest request, ComputedAmounts computedAmounts, Optional<BusinessEntity> businessEntity) {
        String buyerOib = businessEntity.map(BusinessEntity::oib).orElse(request.getBuyerOib());
        String buyerName = businessEntity.map(BusinessEntity::name).orElse(request.getBuyerName());
        String buyerStreet = businessEntity.map(BusinessEntity::headquatersAddress).orElse(request.getBuyerAddress());
        String buyerCity = businessEntity.map(BusinessEntity::headquatersCity).orElse(request.getBuyerCity());
        String buyerPostalZone = businessEntity.map(BusinessEntity::headquatersZip).orElse(request.getBuyerPostalCode());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        PaymentParty buyerParty = PaymentParty.builder()
                .name(buyerName)
                .address(buyerStreet)
                .city(buyerCity)
                .postalZone(buyerPostalZone)
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
                .recipientName(buyerName)
                .recipientOib(buyerOib)
                .recipientAddress(buyerStreet)
                .recipientCity(buyerCity)
                .recipientPost(buyerPostalZone)
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

    private List<UblAdditionalDocumentReference> buildAdditionalDocumentReferences(F2BillRequest request, ComputedAmounts computed, Optional<BusinessEntity> businessEntity) {
        List<UblAdditionalDocumentReference> references = new ArrayList<>();
        references.add(UblAdditionalDocumentReference.builder()
                .id("1")
                .attachment(UblAdditionalDocumentReference.Attachment.builder()
                        .embeddedDocumentBinaryObject(createXmlReport(request, computed, businessEntity))
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