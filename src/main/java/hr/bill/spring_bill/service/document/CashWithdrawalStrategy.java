package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dao.CashWithdrawalBalanceRepository;
import hr.bill.spring_bill.dao.TenantPropertyRepository;
import hr.bill.spring_bill.dto.bill_pdf.request.DepositRequest;
import hr.bill.spring_bill.dto.bill_pdf.request.DisbursementRequest;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import hr.bill.spring_bill.model.TenantPropertyEntity;
import hr.bill.spring_bill.model.enums.TenantPropety;
import hr.bill.spring_bill.service.NumberToWordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashWithdrawalStrategy implements DocumentStrategy {

    private final CashWithdrawalBalanceRepository cashWithdrawalBalanceRepository;

    private final TenantPropertyRepository tenantPropertyRepository;

    private final BillPdfClient billPdfClient;

    private final SupplierProperties supplierProperties;

    @Override
    public BillReportType getType() {
        return BillReportType.CASH_WITHDRAWAL;
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for cash withdrawal {}", id);
        CashWithdrawalBalanceEntity entity = cashWithdrawalBalanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Cash withdrawal not found for id: " + id));
        Map<TenantPropety, TenantPropertyEntity> tenantPropertyMap = tenantPropertyRepository.
                findByPropertyIn(List.of(TenantPropety.DEPOSIT_COUNT, TenantPropety.DISBURSEMENT_COUNT)).stream().
                collect(Collectors.toMap(TenantPropertyEntity::getProperty, Function.identity()));

        int disbursementNumber = increment(tenantPropertyMap, TenantPropety.DISBURSEMENT_COUNT);
        int depositNumber = increment(tenantPropertyMap, TenantPropety.DEPOSIT_COUNT);

        byte[] disbursement = billPdfClient.renderDisbursement(DisbursementRequest.builder()
                .disbursementNumber(String.valueOf(disbursementNumber))
                .amount(formatAmount(entity.getTotal()))
                .amountInWords(NumberToWordsService.asWords(entity.getTotal()))
                .day(formatDate(entity.getBankTransaction().getTransactionDate()))
                .place(supplierProperties.city())
                .purpose("materijalni troškovi")
                .recipientName(supplierProperties.contactName())
                .year(formatYear(entity.getBankTransaction().getTransactionDate()))
                .build());
        byte[] deposit = billPdfClient.renderDeposit(DepositRequest.builder()
                .depositNumber(String.valueOf(depositNumber))
                .amount(formatAmount(entity.getTotal()))
                .amountInWords(NumberToWordsService.asWords(entity.getTotal()))
                .amountReceived("MASTERCARD")
                .purpose("materijalni troškovi")
                .place(supplierProperties.city())
                .day(formatDate(entity.getBankTransaction().getTransactionDate()))
                .year(formatYear(entity.getBankTransaction().getTransactionDate()))
                .build());

        return BillDocument.builder()
                .content(merge(deposit, disbursement))
                .filename("cash-withdrawal-" + entity.getId())
                .build();
    }

    private int increment(Map<TenantPropety, TenantPropertyEntity> tenantPropertyMap, TenantPropety property) {
        TenantPropertyEntity entity = tenantPropertyMap.computeIfAbsent(property,
                p -> TenantPropertyEntity.builder().property(p).value("0").build());
        int next = Integer.parseInt(entity.getValue()) + 1;
        entity.setValue(String.valueOf(next));
        tenantPropertyRepository.save(entity);
        return next;
    }

    private byte[] merge(byte[]... pdfs) {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            merger.setDestinationStream(mergedOutput);
            for (byte[] pdf : pdfs) {
                merger.addSource(new RandomAccessReadBuffer(pdf));
            }
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return mergedOutput.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("hr"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern("dd.MM."));
    }

    private String formatYear(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern("yy"));
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for cash withdrawal {}", id);
        CashWithdrawalBalanceEntity entity = cashWithdrawalBalanceRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Cash withdrawal not found for id: " + id));
        entity.setSentCount(entity.getSentCount() + 1);
        cashWithdrawalBalanceRepository.save(entity);
    }
}
