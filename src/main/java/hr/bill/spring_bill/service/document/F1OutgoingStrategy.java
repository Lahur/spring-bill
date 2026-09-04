package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.eposlovanje_util.EposlovanjeUtilClient;
import hr.bill.spring_bill.clients.f1_web.F1WebClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.BillRepository;
import hr.bill.spring_bill.dto.bill_pdf.request.BillRequest;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.response.ApiResponse;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.ReceiptType;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.UnitOfMeasure;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.CreateReceiptDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.CreateReceiptItemDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.request.GetReceiptsQuery;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.FiscalizationResultDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptListResultDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptSummaryDto;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.dto.web.bill.BaseBillRequest;
import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.BillSearchParams;
import hr.bill.spring_bill.dto.web.bill.b2c.F1BillRequest;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.*;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillDocumentStatus;
import hr.bill.spring_bill.model.enums.BillType;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import hr.bill.spring_bill.service.CroatianTimeZone;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class F1OutgoingStrategy implements BillStrategy {

    private final F1WebClient f1WebClient;

    private final EposlovanjeUtilClient eposlovanjeUtilClient;

    private final BillPdfClient billPdfClient;

    private final PaymentInfoMapper paymentInfoMapper;

    private final ReceiptBillRequestMapper receiptBillRequestMapper;

    private final BillEntityMapper billEntityMapper;

    private final BillInfoMapper billInfoMapper;

    private final SupplierProperties supplierProperties;

    private final PaymentReferenceMatcher paymentReferenceMatcher;

    @Value("${bill.schedule.sync-lookback-weeks}")
    private long syncLookbackWeeks;


    private final BillRepository repository;

    @Override
    public BillReportType getType() {
        return BillReportType.F1_OUTGOING;
    }

    @Override
    public List<BillResponse> getBills() {
        List<BillEntity> bills = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F1_BILL);
        return billEntityMapper.toBillResponseList(bills);
    }

    @Override
    public List<BillResponse> getBillsFilter(BillSearchParams params) {
        LocalDateTime from = params.dateFrom() == null ? null : params.dateFrom().atStartOfDay();
        LocalDateTime to = params.dateTill() == null ? null : params.dateTill().plusDays(1).atStartOfDay();
        List<ReceiptSummaryDto> receipts = f1WebClient.getReceipts(GetReceiptsQuery.builder()
                        .dateFrom(from == null ? null : from.format(DateTimeFormatter.ISO_DATE_TIME))
                        .dateTo(to == null ? null : to.format(DateTimeFormatter.ISO_DATE_TIME))
                        .build())
                .items().stream()
                .filter(ri -> {
                    LocalDateTime issueDateTime = LocalDateTime.parse(ri.issueDateTime());
                    return (from == null || !issueDateTime.isBefore(from))
                            && (to == null || issueDateTime.isBefore(to));
                })
                .toList();
        List<BillResponse> bills = receipts.stream()
                .map(ri -> f1WebClient.getReceipt(ri.id()))
                .map(receiptDto -> billEntityMapper.toBillResponse(receiptDto, BillType.F1_BILL))
                .toList();
        return paymentReferenceMatcher.markPaidByBillSystemId(bills);
    }

    @Override
    public PaidUnpaidTotals getMonthlyTotals(LocalDate monthStart) {
        log.debug("Computing F1 monthly totals for {}", monthStart);
        LocalDateTime from = monthStart.atStartOfDay();
        LocalDateTime to = monthStart.plusMonths(1).atStartOfDay();
        List<ReceiptSummaryDto> receipts = f1WebClient.getReceiptsByDateRange(
                from.format(DateTimeFormatter.ISO_DATE_TIME),
                to.format(DateTimeFormatter.ISO_DATE_TIME));
        Set<String> paidReferences = paymentReferenceMatcher.paidReferences(CreditDebitIndicator.CRDT, supplierProperties.iban());
        Set<String> paidBillSystemIds = paymentReferenceMatcher.paidBillSystemIds(receipts.stream()
                .map(r -> String.valueOf(r.id()))
                .toList());
        // The F1 web API's date-range filter isn't reliable, so re-check locally before summing,
        // the same way sync() re-validates results against its threshold.
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal unpaid = BigDecimal.ZERO;
        for (ReceiptSummaryDto r : receipts) {
            LocalDateTime issueDateTime = LocalDateTime.parse(r.issueDateTime());
            if (issueDateTime.isBefore(from) || !issueDateTime.isBefore(to)) continue;
            BigDecimal amount = BigDecimal.valueOf(r.grandTotal());
            if (paidBillSystemIds.contains(String.valueOf(r.id()))
                    || paymentReferenceMatcher.isPaid(paidReferences, r.formattedReceiptNumber())) {
                paid = paid.add(amount);
            } else {
                unpaid = unpaid.add(amount);
            }
        }
        return new PaidUnpaidTotals(paid, unpaid);
    }

    @Override
    public Optional<LocalDate> findEarliestBillMonth() {
        return f1WebClient.getReceiptsByDateRange(null, null).stream()
                .map(r -> LocalDateTime.parse(r.issueDateTime()))
                .min(LocalDateTime::compareTo)
                .map(dt -> dt.toLocalDate().withDayOfMonth(1));
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for F1 bill {}", id);
        ReceiptDto receiptDto = f1WebClient.getReceipt(Integer.parseInt(id));
        ApiResponse apiResponse = eposlovanjeUtilClient.generatePdf417(paymentInfoMapper.toPaymentInfo(
                supplierProperties, receiptDto, "HR00"));

        BillRequest billRequest = receiptBillRequestMapper.toBillRequest(receiptDto, apiResponse.message());
        return BillDocument.builder()
                .filename(receiptDto.formattedReceiptNumber().replace("/", "-").replace("\\", "-"))
                .content(billPdfClient.renderBill(billRequest))
                .build();
    }

    @Override
    public BillResponse createBill(BaseBillRequest request) {
        if(request instanceof F1BillRequest f1BillRequest) {
            log.info("Creating F1 bill for buyer OIB {}", f1BillRequest.getBuyerOib());
            ReceiptDto receiptDto = f1WebClient.createReceipt(CreateReceiptDto.builder()
                    .businessId(17234)
                    .issueDateTime(LocalDateTime.of(f1BillRequest.getBillDate(), f1BillRequest.getBillTime())
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .paymentMethod(PaymentMethod.BankTransfer)
                    .receiptType(ReceiptType.Standard)
                    .operatorOib(supplierProperties.contactOib())
                    .notes(f1BillRequest.getNote() == null || f1BillRequest.getNote().isBlank() ? null : f1BillRequest.getNote())
                    .paymentDueDate(f1BillRequest.getDueDate().toString())
                    .buyerName(f1BillRequest.getBuyerName())
                    .buyerOib(f1BillRequest.getBuyerOib())
                    .buyerAddress(f1BillRequest.getBuyerAddress())
                    .buyerCity(f1BillRequest.getBuyerCity())
                    .buyerPostalCode(f1BillRequest.getBuyerPostalCode())
                    .items(List.of(CreateReceiptItemDto.builder()
                            .name(f1BillRequest.getBillItemName())
                            .description(f1BillRequest.getBillItemDescription())
                            .quantity(1.0)
                            .unitPrice(f1BillRequest.getBaseAmount().doubleValue())
                            .taxRate(f1BillRequest.getTaxRate())
                            .unitOfMeasure(UnitOfMeasure.Kom)
                            .discountAmount(0.0)
                            .discountPercent(0.0)
                            .build()))
                    .autoFiscalize(true)
                    .build());
            BillEntity billEntity = repository.save(billEntityMapper.toBillEntity(receiptDto, BillType.F1_BILL));
            log.info("Created F1 bill {}", billEntity.getFullBillId());
            return billEntityMapper.toBillResponse(billEntity);
        }
        return null;
    }

    @Override
    public BillInfoResponse getBillInfo(String id) {
        log.debug("Fetching bill info for F1 bill {}", id);
        ReceiptDto receiptDto = f1WebClient.getReceipt(Integer.parseInt(id));
        return billInfoMapper.toBillInfoResponse(supplierProperties, receiptDto);
    }

    @Override
    public BillResponse cancel(String originalId, String newId) {
        log.info("Cancelling F1 bill {} with replacement {}", originalId, newId);
        FiscalizationResultDto cancelResponse = f1WebClient.storno(Integer.parseInt(originalId));
        BillEntity billEntity = repository.save(billEntityMapper.toBillEntity(cancelResponse.receipt(), BillType.F1_BILL));
        return billEntityMapper.toBillResponse(billEntity);
    }

    @Override
    public BillReviewResponse reviewBill(BaseBillRequest request) {
        if (request instanceof F1BillRequest f1BillRequest) {
            log.debug("Reviewing F1 bill request for buyer OIB {}", f1BillRequest.getBuyerOib());
            ReceiptListResultDto lastBills = f1WebClient.getReceipts(GetReceiptsQuery.builder()
                    .page(1)
                    .pageSize(1)
                    .searchTerm("/2")
                    .sortDescending(true)
                    .build());
            int receiptNumber = lastBills.items().stream()
                    .findFirst()
                    .map(ReceiptSummaryDto::receiptNumber)
                    .orElse(0);
            ComputedAmounts computedAmounts = computeAmounts(f1BillRequest);
            return BillReviewResponse.builder()
                    .billNumber(String.format("%d/1/2", receiptNumber + 1))
                    .billDate(f1BillRequest.getBillDate())
                    .billTime(f1BillRequest.getBillTime())
                    .dueDate(f1BillRequest.getDueDate())
                    .name(f1BillRequest.getBillItemName())
                    .description(f1BillRequest.getBillItemDescription())
                    .note(f1BillRequest.getNote())
                    .buyerOib(f1BillRequest.getBuyerOib())
                    .buyerName(f1BillRequest.getBuyerName())
                    .buyerAddress(f1BillRequest.getBuyerAddress())
                    .buyerCity(f1BillRequest.getBuyerCity())
                    .buyerPostalZone(f1BillRequest.getBuyerPostalCode())
                    .baseAmount(computedAmounts.baseAmount())
                    .taxAmount(computedAmounts.taxAmount())
                    .totalAmount(computedAmounts.totalAmount())
                    .build();
        }
        return null;
    }

    @Override
    public void sync() {
        log.debug("Syncing F1 bills");
        LocalDateTime threshold = repository.findFirstByBillTypeOrderByBillDateDesc(BillType.F1_BILL)
                .map(b -> b.getBillDate().plusMinutes(10))
                .orElseGet(() -> LocalDate.now(CroatianTimeZone.ZONE).withDayOfMonth(1).minusWeeks(syncLookbackWeeks).atStartOfDay());
        GetReceiptsQuery query = GetReceiptsQuery.builder()
                .dateFrom(threshold.format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
        List<ReceiptSummaryDto> toSync = f1WebClient.getReceipts(query).items().stream()
                .filter(ri -> LocalDateTime.parse(ri.issueDateTime()).isAfter(threshold))
                .toList();
        toSync.forEach((ri) -> {
            ReceiptDto receiptDto = f1WebClient.getReceipt(ri.id());
            repository.save(billEntityMapper.toBillEntity(receiptDto, BillType.F1_BILL));
        });
        log.info("Synced {} F1 bill(s)", toSync.size());
    }

    @Override
    public void deleteAll() {
        log.debug("Deleting all F1 bills");
        repository.deleteAllByBillType(BillType.F1_BILL);
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for F1 bill {}", id);
        BillEntity billEntity = repository.findBySystemIdAndBillType(Long.parseLong(id), BillType.F1_BILL)
                .orElseThrow(() -> new NotFoundException("Bill not found for id: " + id));
        billEntity.setSentCount(billEntity.getSentCount() + 1);
        repository.save(billEntity);
    }

    @Override
    public int markPaidFromBankStatement(List<BankTransactionEntity> transactions) {
        List<BillEntity> candidates = repository.findAllByBillTypeOrderByBillDateDesc(BillType.F1_BILL);
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
        log.info("Marked {} F1 bill(s) as paid from bank statement", updated);
        return updated;
    }

    @Override
    public RemoteMatchResult matchBillSystemIdsFromRemote(List<BankTransactionEntity> unresolvedTransactions, LocalDate from, LocalDate to) {
        List<BankTransactionEntity> candidates = unresolvedTransactions.stream()
                .filter(tx -> tx.getCreditDebitIndicator() == CreditDebitIndicator.CRDT)
                .filter(tx -> supplierProperties.iban().equalsIgnoreCase(tx.getReceiverIban()))
                .toList();
        if (candidates.isEmpty()) {
            return RemoteMatchResult.empty();
        }
        // F1 receipts carry no payment status, so every receipt in the window is a match candidate.
        List<ReceiptSummaryDto> receipts = f1WebClient.getReceiptsByDateRange(
                from == null ? null : from.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                to == null ? null : to.plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        int updated = 0;
        Set<LocalDate> updatedMonths = new HashSet<>();
        for (BankTransactionEntity tx : candidates) {
            for (ReceiptSummaryDto receipt : receipts) {
                if (HrPaymentReferenceService.referencesMatch(tx.getReference(), receipt.formattedReceiptNumber())) {
                    tx.setBillSystemId(String.valueOf(receipt.id()));
                    markLocalBillPaid(receipt.id().longValue());
                    updatedMonths.add(LocalDateTime.parse(receipt.issueDateTime()).toLocalDate().withDayOfMonth(1));
                    updated++;
                    break;
                }
            }
        }
        log.info("Resolved {} bank transaction(s) against upstream F1 receipts", updated);
        return new RemoteMatchResult(updated, updatedMonths);
    }

    private void markLocalBillPaid(long systemId) {
        repository.findBySystemIdAndBillType(systemId, BillType.F1_BILL).ifPresent(bill -> {
            bill.setDocumentStatus(BillDocumentStatus.PlacenUPotpunosti);
            repository.save(bill);
        });
    }

    private String expectedReference(BillEntity bill) {
        return bill.getPaymentReference() != null
                ? bill.getPaymentReference()
                : HrPaymentReferenceService.buildReference(bill.getFullBillId());
    }

    private ComputedAmounts computeAmounts(F1BillRequest request) {
        BigDecimal baseAmount = request.getBaseAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = baseAmount.multiply(BigDecimal.valueOf(request.getTaxRate().getValue()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(taxAmount);
        return new ComputedAmounts(baseAmount, taxAmount, totalAmount);
    }

    private record ComputedAmounts(BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal totalAmount) {}
}