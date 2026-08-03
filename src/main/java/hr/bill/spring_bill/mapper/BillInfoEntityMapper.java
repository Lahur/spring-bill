package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.bill_pdf.common.InvoiceLineDto;
import hr.bill.spring_bill.dto.bill_pdf.common.MonetaryTotalDto;
import hr.bill.spring_bill.dto.bill_pdf.common.OutgoingCustomerDto;
import hr.bill.spring_bill.dto.bill_pdf.common.OutgoingPaymentMeansDto;
import hr.bill.spring_bill.dto.bill_pdf.common.OutgoingSupplierDto;
import hr.bill.spring_bill.dto.bill_pdf.common.TaxSubtotalDto;
import hr.bill.spring_bill.dto.bill_pdf.common.BillDetailsDto;
import hr.bill.spring_bill.dto.bill_pdf.request.BillWithDetailsRequest;
import hr.bill.spring_bill.dto.eposlovanje.enums.DocumentType;
import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
import hr.bill.spring_bill.dto.eposlovanje.enums.UnitOfMeasure;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.BillInfoEntity;
import hr.bill.spring_bill.model.BillItemEntity;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.xml.ubl.model.UblInvoice;
import hr.bill.spring_bill.xml.ubl.model.UblInvoiceLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {
        DocumentType.class, PaymentMeans.class, VatCategory.class, UnitOfMeasure.class,
        LocalDate.class, BigDecimal.class, HrPaymentReferenceService.class
})
public interface BillInfoEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "billId", target = "billId")
    @Mapping(target = "mainBillDate", expression = "java(LocalDate.parse(inv.getIssueDate()))")
    @Mapping(target = "mainDueDate", expression = "java(LocalDate.parse(inv.getDueDate()))")
    @Mapping(target = "documentType", expression = "java(DocumentType.fromCode(inv.getInvoiceTypeCode()))")
    @Mapping(source = "inv.documentCurrencyCode", target = "currency")
    @Mapping(target = "billPeriodFrom", expression = "java(inv.getInvoicePeriod() != null ? LocalDate.parse(inv.getInvoicePeriod().getStartDate()) : null)")
    @Mapping(target = "billPeriodTill", expression = "java(inv.getInvoicePeriod() != null ? LocalDate.parse(inv.getInvoicePeriod().getEndDate()) : null)")
    @Mapping(source = "inv.accountingCustomerParty.party.partyName.name", target = "buyerName")
    @Mapping(source = "inv.accountingCustomerParty.party.partyLegalEntity.companyId", target = "buyerOib")
    @Mapping(source = "inv.accountingCustomerParty.party.postalAddress.streetName", target = "buyerAddress")
    @Mapping(source = "inv.accountingCustomerParty.party.postalAddress.cityName", target = "buyerCity")
    @Mapping(source = "inv.accountingCustomerParty.party.postalAddress.postalZone", target = "buyerPostalCode")
    @Mapping(source = "inv.accountingSupplierParty.party.partyName.name", target = "supplierName")
    @Mapping(source = "inv.accountingSupplierParty.party.partyLegalEntity.companyId", target = "supplierOib")
    @Mapping(source = "inv.accountingSupplierParty.party.postalAddress.streetName", target = "supplierAddress")
    @Mapping(source = "inv.accountingSupplierParty.party.postalAddress.cityName", target = "supplierCity")
    @Mapping(source = "inv.accountingSupplierParty.party.postalAddress.postalZone", target = "supplierPostalCode")
    @Mapping(source = "inv.accountingSupplierParty.party.contact.name", target = "supplierContactName")
    @Mapping(source = "inv.accountingSupplierParty.sellerContact.id", target = "supplierContactOib")
    @Mapping(source = "inv.accountingSupplierParty.party.contact.electronicMail", target = "supplierContactEmail")
    @Mapping(source = "inv.accountingSupplierParty.party.contact.telephone", target = "supplierContactPhone")
    @Mapping(target = "paymentMeans", expression = "java(PaymentMeans.fromCode(inv.getPaymentMeans().getPaymentMeansCode()))")
    @Mapping(target = "paymentDueDate", expression = "java(LocalDate.parse(inv.getPaymentMeans().getPaymentDueDate()))")
    @Mapping(source = "inv.paymentMeans.payeeFinancialAccount.id", target = "paymentIban")
    @Mapping(target = "paymentModel", expression = "java(HrPaymentReferenceService.extractHrModel(inv.getPaymentMeans().getPaymentId()))")
    @Mapping(target = "paymentReference", expression = "java(HrPaymentReferenceService.trimHrPrefix(inv.getPaymentMeans().getPaymentId()))")
    @Mapping(source = "inv.paymentMeans.instructionNote", target = "paymentNote")
    @Mapping(target = "vatExclusiveAmount", expression = "java(new BigDecimal(inv.getLegalMonetaryTotal().getTaxExclusiveAmount().getValue().trim()))")
    @Mapping(target = "vatAmount", expression = "java(new BigDecimal(inv.getTaxTotal().getTaxAmount().getValue().trim()))")
    @Mapping(target = "vatInclusiveAmount", expression = "java(new BigDecimal(inv.getLegalMonetaryTotal().getTaxInclusiveAmount().getValue().trim()))")
    @Mapping(target = "advanceAmount", expression = "java(new BigDecimal(inv.getLegalMonetaryTotal().getPrepaidAmount().getValue().trim()))")
    @Mapping(target = "totalAmount", expression = "java(new BigDecimal(inv.getLegalMonetaryTotal().getPayableAmount().getValue().trim()))")
    BillInfoEntity toBillInfoEntity(UblInvoice inv, UUID billId);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "billInfoId", target = "billInfoId")
    @Mapping(target = "itemOrder", expression = "java(lineOrder(line))")
    @Mapping(source = "line.item.name", target = "name")
    @Mapping(source = "line.item.description", target = "description")
    @Mapping(target = "quantity", expression = "java(new BigDecimal(line.getInvoicedQuantity().getValue().trim()))")
    @Mapping(target = "unitOfMeasure", expression = "java(UnitOfMeasure.fromInternationalCode(line.getInvoicedQuantity().getUnitCode()))")
    @Mapping(target = "baseAmount", expression = "java(new BigDecimal(line.getLineExtensionAmount().getValue().trim()))")
    @Mapping(target = "totalAmount", expression = "java(lineTotalAmount(line))")
    @Mapping(target = "vatCategory", expression = "java(VatCategory.fromId(line.getItem().getClassifiedTaxCategory().getId()))")
    BillItemEntity toBillItemEntity(UblInvoiceLine line, UUID billInfoId);

    default List<BillItemEntity> toBillItemEntities(List<UblInvoiceLine> lines, UUID billInfoId) {
        return lines.stream().map(line -> toBillItemEntity(line, billInfoId)).toList();
    }

    default Integer lineOrder(UblInvoiceLine line) {
        try {
            return line.getId() != null ? Integer.parseInt(line.getId().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    default BigDecimal lineTotalAmount(UblInvoiceLine line) {
        BigDecimal base = new BigDecimal(line.getLineExtensionAmount().getValue().trim());
        BigDecimal rate = new BigDecimal(line.getItem().getClassifiedTaxCategory().getPercent().trim());
        return base.multiply(BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100))));
    }

    // -------------------------------------------------------------------------
    // BillWithDetailsRequest
    // -------------------------------------------------------------------------

    @Mapping(target = "recipientName", source = "info.buyerName")
    @Mapping(target = "recipientAddress", source = "info.buyerAddress")
    @Mapping(target = "recipientPost", source = "info.buyerPostalCode")
    @Mapping(target = "recipientCity", source = "info.buyerCity")
    @Mapping(target = "recipientOib", source = "info.buyerOib")
    @Mapping(target = "billNumber", source = "bill.fullBillId")
    @Mapping(target = "billDate", expression = "java(formatLocalDate(info.getMainBillDate()))")
    @Mapping(target = "billTime", expression = "java(formatBillTime(bill))")
    @Mapping(target = "billProjectDescription", expression = "java(firstItemDescription(items))")
    @Mapping(target = "billProjectName", expression = "java(firstItemName(items))")
    @Mapping(target = "billBasePrice", expression = "java(formatAmount(info.getVatExclusiveAmount()))")
    @Mapping(target = "billPdvPrice", expression = "java(formatAmount(info.getVatAmount()))")
    @Mapping(target = "billTotalPrice", expression = "java(formatAmount(info.getVatInclusiveAmount()))")
    @Mapping(target = "billTotalText", expression = "java(hr.bill.spring_bill.service.NumberToWordsService.asWords(info.getVatInclusiveAmount()))")
    @Mapping(target = "billReverseCharge", expression = "java(firstItemReverseCharge(items))")
    @Mapping(target = "paymentDays", expression = "java(java.time.temporal.ChronoUnit.DAYS.between(info.getMainDueDate(), info.getMainBillDate()))")
    @Mapping(target = "zki", ignore = true)
    @Mapping(target = "jir", ignore = true)
    @Mapping(target = "details", expression = "java(toBillDetailsDto(bill, info, items))")
    BillWithDetailsRequest toBillWithDetailsRequest(BillEntity bill, BillInfoEntity info, List<BillItemEntity> items, String pdf417Image);

    @Mapping(target = "billNumber", source = "bill.fullBillId")
    @Mapping(target = "billDate", expression = "java(dateStr(info.getMainBillDate()))")
    @Mapping(target = "copyIndicator", ignore = true)
    @Mapping(target = "invoiceTypeCode", expression = "java(info.getDocumentType() != null ? info.getDocumentType().getCode() : null)")
    @Mapping(target = "currencyCode", source = "info.currency")
    @Mapping(target = "dueDate", expression = "java(dateStr(info.getMainDueDate()))")
    @Mapping(target = "periodStart", expression = "java(dateStr(info.getBillPeriodFrom()))")
    @Mapping(target = "periodEnd", expression = "java(dateStr(info.getBillPeriodTill()))")
    @Mapping(target = "orderReferenceId", ignore = true)
    @Mapping(target = "customizationId", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "invoiceNote", source = "info.paymentNote")
    @Mapping(target = "supplier", expression = "java(toOutgoingSupplier(info))")
    @Mapping(target = "customer", expression = "java(toOutgoingCustomer(info))")
    @Mapping(target = "paymentMeans", expression = "java(toOutgoingPaymentMeans(info))")
    @Mapping(target = "lines", expression = "java(toInvoiceLines(items))")
    @Mapping(target = "taxSubtotals", expression = "java(toTaxSubtotalList(items))")
    @Mapping(target = "taxTotalAmount", expression = "java(plainAmount(info.getVatAmount()))")
    @Mapping(target = "monetary", expression = "java(toMonetaryTotal(info))")
    BillDetailsDto toBillDetailsDto(BillEntity bill, BillInfoEntity info, List<BillItemEntity> items);

    @Mapping(target = "name", source = "supplierName")
    @Mapping(target = "oib", source = "supplierOib")
    @Mapping(target = "street", source = "supplierAddress")
    @Mapping(target = "city", source = "supplierCity")
    @Mapping(target = "postalZone", source = "supplierPostalCode")
    @Mapping(target = "countryCode", ignore = true)
    @Mapping(target = "contactName", source = "supplierContactName")
    @Mapping(target = "contactOib", source = "supplierContactOib")
    @Mapping(target = "phone", source = "supplierContactPhone")
    @Mapping(target = "email", source = "supplierContactEmail")
    OutgoingSupplierDto toOutgoingSupplier(BillInfoEntity entity);

    @Mapping(target = "name", source = "buyerName")
    @Mapping(target = "oib", source = "buyerOib")
    @Mapping(target = "street", source = "buyerAddress")
    @Mapping(target = "city", source = "buyerCity")
    @Mapping(target = "postalZone", source = "buyerPostalCode")
    @Mapping(target = "countryCode", ignore = true)
    OutgoingCustomerDto toOutgoingCustomer(BillInfoEntity entity);

    @Mapping(target = "code", expression = "java(entity.getPaymentMeans() != null ? entity.getPaymentMeans().getCode() : null)")
    @Mapping(target = "dueDate", expression = "java(dateStr(entity.getPaymentDueDate()))")
    @Mapping(target = "channelCode", ignore = true)
    @Mapping(target = "instructionNote", source = "paymentNote")
    @Mapping(target = "paymentId", expression = "java(paymentId(entity))")
    @Mapping(target = "iban", source = "paymentIban")
    @Mapping(target = "accountCurrencyCode", source = "currency")
    OutgoingPaymentMeansDto toOutgoingPaymentMeans(BillInfoEntity entity);

    @Mapping(target = "lineExtensionAmount", expression = "java(plainAmount(entity.getVatExclusiveAmount()))")
    @Mapping(target = "taxExclusiveAmount", expression = "java(plainAmount(entity.getVatExclusiveAmount()))")
    @Mapping(target = "taxInclusiveAmount", expression = "java(plainAmount(entity.getVatInclusiveAmount()))")
    @Mapping(target = "prepaidAmount", expression = "java(plainAmount(entity.getAdvanceAmount()))")
    @Mapping(target = "payableAmount", expression = "java(plainAmount(payableAmount(entity)))")
    MonetaryTotalDto toMonetaryTotal(BillInfoEntity entity);

    @Mapping(target = "id", source = "itemOrder")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "classificationCode", ignore = true)
    @Mapping(target = "quantity", expression = "java(plainAmount(item.getQuantity()))")
    @Mapping(target = "unitCode", expression = "java(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getInternationalCode() : null)")
    @Mapping(target = "unitPrice", expression = "java(unitPrice(item))")
    @Mapping(target = "lineExtensionAmount", expression = "java(plainAmount(item.getBaseAmount()))")
    @Mapping(target = "vatCategory", expression = "java(item.getVatCategory() != null ? item.getVatCategory().getId() : null)")
    InvoiceLineDto toInvoiceLine(BillItemEntity item);

    default List<InvoiceLineDto> toInvoiceLines(List<BillItemEntity> items) {
        return items == null ? List.of() : sortedItems(items).stream().map(this::toInvoiceLine).toList();
    }

    default List<TaxSubtotalDto> toTaxSubtotalList(List<BillItemEntity> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream()
                .collect(Collectors.groupingBy(BillItemEntity::getVatCategory))
                .entrySet().stream()
                .map(e -> {
                    VatCategory cat = e.getKey();
                    BigDecimal taxable = e.getValue().stream()
                            .map(BillItemEntity::getBaseAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal tax = e.getValue().stream()
                            .map(i -> i.getTotalAmount() != null && i.getBaseAmount() != null
                                    ? i.getTotalAmount().subtract(i.getBaseAmount())
                                    : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TaxSubtotalDto(
                            cat != null ? cat.getId() : null,
                            cat != null ? fmtRate(cat.getRate()) : null,
                            taxable.toPlainString(),
                            tax.toPlainString(),
                            cat != null ? cat.getTaxExemptionReason() : null
                    );
                })
                .toList();
    }

    default List<BillItemEntity> sortedItems(List<BillItemEntity> items) {
        return items.stream().sorted(Comparator.comparing(BillItemEntity::getItemOrder)).toList();
    }

    default String firstItemName(List<BillItemEntity> items) {
        return items == null || items.isEmpty() ? null : sortedItems(items).getFirst().getName();
    }

    default String firstItemDescription(List<BillItemEntity> items) {
        return items == null || items.isEmpty() ? null : sortedItems(items).getFirst().getDescription();
    }

    default Boolean firstItemReverseCharge(List<BillItemEntity> items) {
        if (items == null || items.isEmpty()) return Boolean.FALSE;
        return sortedItems(items).getFirst().getVatCategory() == VatCategory.ReverseCharge;
    }

    default String unitPrice(BillItemEntity item) {
        if (item.getBaseAmount() == null) return null;
        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return item.getBaseAmount().toPlainString();
        }
        return item.getBaseAmount().divide(item.getQuantity(), 2, RoundingMode.HALF_UP).toPlainString();
    }

    default String paymentId(BillInfoEntity entity) {
        if (entity.getPaymentModel() == null && entity.getPaymentReference() == null) return null;
        String model = entity.getPaymentModel() != null ? entity.getPaymentModel() : "";
        String reference = entity.getPaymentReference() != null ? entity.getPaymentReference() : "";
        return (model + " " + reference).trim();
    }

    default BigDecimal payableAmount(BillInfoEntity entity) {
        if (entity.getVatInclusiveAmount() == null) return null;
        BigDecimal advance = entity.getAdvanceAmount() != null ? entity.getAdvanceAmount() : BigDecimal.ZERO;
        return entity.getVatInclusiveAmount().subtract(advance);
    }

    default String fmtRate(BigDecimal rate) {
        if (rate == null) return null;
        if (rate.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return String.format("%.1f", rate);
        }
        return rate.stripTrailingZeros().toPlainString();
    }

    default String formatLocalDate(LocalDate d) {
        return d == null ? null : d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
    }

    default String formatBillTime(BillEntity bill) {
        return bill.getBillDate() == null ? null : bill.getBillDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    default String dateStr(LocalDate d) {
        return d == null ? null : d.toString();
    }

    default String formatAmount(BigDecimal amount) {
        return amount == null ? null : amount.toPlainString().replace('.', ',');
    }

    default String plainAmount(BigDecimal amount) {
        return amount == null ? null : amount.toPlainString();
    }
}