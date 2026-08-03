package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dto.eposlovanje.enums.DocumentType;
import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.ReceiptItemDto;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptDto;
import hr.bill.spring_bill.dto.web.bill.info.BillDocumentKind;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.dto.web.bill.info.BillItemInfo;
import hr.bill.spring_bill.dto.web.bill.info.BillPaymentMethod;
import hr.bill.spring_bill.dto.web.bill.info.BillVatRate;
import hr.bill.spring_bill.dto.web.bill.info.BuyerInfo;
import hr.bill.spring_bill.dto.web.bill.info.FiscalInfo;
import hr.bill.spring_bill.dto.web.bill.info.ItemUnitOfMeasure;
import hr.bill.spring_bill.dto.web.bill.info.MainDataInfo;
import hr.bill.spring_bill.dto.web.bill.info.PaymentInfo;
import hr.bill.spring_bill.dto.web.bill.info.PriceInfo;
import hr.bill.spring_bill.dto.web.bill.info.SupplierInfo;
import hr.bill.spring_bill.model.BillInfoEntity;
import hr.bill.spring_bill.model.BillItemEntity;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.xml.ubl.model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {
                DocumentType.class, PaymentMeans.class, VatCategory.class, LocalDate.class, BigDecimal.class,
                BillDocumentKind.class, BillPaymentMethod.class, BillVatRate.class, ItemUnitOfMeasure.class
        }
)
public interface BillInfoMapper {

    @Mapping(target = "mainDataInfo", expression = "java(toMainDataInfo(inv))")
    @Mapping(source = "accountingCustomerParty", target = "buyerInfo")
    @Mapping(source = "accountingSupplierParty", target = "supplierInfo")
    @Mapping(source = "invoiceLines", target = "itemInfos")
    @Mapping(target = "paymentInfo", expression = "java(toPaymentInfo(inv.getPaymentMeans(), inv.getDueDate()))")
    @Mapping(target = "fiscalInfo", ignore = true)
    @Mapping(target = "priceInfo", expression = "java(toPriceInfo(inv.getLegalMonetaryTotal(), inv.getTaxTotal()))")
    BillInfoResponse toBillInfoResponse(UblInvoice inv);

    @Mapping(target = "mainDataInfo", expression = "java(toMainDataInfo(r))")
    @Mapping(target = "buyerInfo", expression = "java(toBuyerInfo(r))")
    @Mapping(target = "supplierInfo", expression = "java(toSupplierInfo(supplier, r))")
    @Mapping(source = "r.items", target = "itemInfos")
    @Mapping(target = "paymentInfo", ignore = true)
    @Mapping(target = "fiscalInfo", expression = "java(toFiscalInfo(r))")
    @Mapping(target = "priceInfo", expression = "java(toPriceInfo(r))")
    BillInfoResponse toBillInfoResponse(SupplierProperties supplier, ReceiptDto r);

    @Mapping(target = "mainDataInfo", expression = "java(toMainDataInfo(entity))")
    @Mapping(target = "buyerInfo", expression = "java(toBuyerInfo(entity))")
    @Mapping(target = "supplierInfo", expression = "java(toSupplierInfo(entity))")
    @Mapping(source = "items", target = "itemInfos")
    @Mapping(target = "paymentInfo", expression = "java(toPaymentInfo(entity))")
    @Mapping(target = "fiscalInfo", ignore = true)
    @Mapping(target = "priceInfo", expression = "java(toPriceInfo(entity))")
    BillInfoResponse toBillInfoResponse(BillInfoEntity entity, List<BillItemEntity> items);

    @Mapping(target = "billDate", expression = "java(LocalDate.parse(inv.getIssueDate()))")
    @Mapping(target = "dueDate", expression = "java(LocalDate.parse(inv.getDueDate()))")
    @Mapping(target = "documentKind", expression = "java(BillDocumentKind.fromDocumentType(DocumentType.fromCode(inv.getInvoiceTypeCode())))")
    @Mapping(source = "documentCurrencyCode", target = "currency")
    @Mapping(target = "billPeriodFrom", expression = "java(inv.getInvoicePeriod() != null ? LocalDate.parse(inv.getInvoicePeriod().getStartDate()) : null)")
    @Mapping(target = "billPeriodTill", expression = "java(inv.getInvoicePeriod() != null ? LocalDate.parse(inv.getInvoicePeriod().getEndDate()) : null)")
    @Mapping(target = "issueDateTime", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "fiscalStatus", ignore = true)
    MainDataInfo toMainDataInfo(UblInvoice inv);

    @Mapping(target = "issueDateTime", expression = "java(java.time.LocalDateTime.parse(r.issueDateTime()))")
    @Mapping(target = "dueDate", expression = "java(r.paymentDueDate() != null ? java.time.LocalDateTime.parse(r.paymentDueDate()).toLocalDate() : null)")
    @Mapping(target = "billDate", ignore = true)
    @Mapping(target = "documentKind", expression = "java(BillDocumentKind.fromReceiptType(r.receiptType()))")
    @Mapping(target = "paymentMethod", expression = "java(BillPaymentMethod.fromPaymentMethod(r.paymentMethod()))")
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "billPeriodFrom", ignore = true)
    @Mapping(target = "billPeriodTill", ignore = true)
    MainDataInfo toMainDataInfo(ReceiptDto r);

    @Mapping(source = "mainBillDate", target = "billDate")
    @Mapping(source = "mainDueDate", target = "dueDate")
    @Mapping(target = "documentKind", expression = "java(BillDocumentKind.fromDocumentType(entity.getDocumentType()))")
    @Mapping(target = "issueDateTime", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "fiscalStatus", ignore = true)
    MainDataInfo toMainDataInfo(BillInfoEntity entity);

    @Mapping(source = "party.partyName.name", target = "name")
    @Mapping(source = "party.partyTaxScheme.companyId", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "address")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalCode")
    BuyerInfo toBuyerInfo(UblAccountingCustomerParty customer);

    @Mapping(source = "buyerName", target = "name")
    @Mapping(source = "buyerOib", target = "oib")
    @Mapping(source = "buyerAddress", target = "address")
    @Mapping(source = "buyerCity", target = "city")
    @Mapping(source = "buyerPostalCode", target = "postalCode")
    BuyerInfo toBuyerInfo(ReceiptDto r);

    @Mapping(source = "buyerName", target = "name")
    @Mapping(source = "buyerOib", target = "oib")
    @Mapping(source = "buyerAddress", target = "address")
    @Mapping(source = "buyerCity", target = "city")
    @Mapping(source = "buyerPostalCode", target = "postalCode")
    BuyerInfo toBuyerInfo(BillInfoEntity entity);

    @Mapping(source = "party.partyName.name", target = "name")
    @Mapping(source = "party.partyTaxScheme.companyId", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "address")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalCode")
    @Mapping(source = "party.contact.name", target = "contactName")
    @Mapping(source = "sellerContact.id", target = "contactOib")
    @Mapping(source = "party.contact.electronicMail", target = "contactEmail")
    @Mapping(source = "party.contact.telephone", target = "contactPhone")
    SupplierInfo toSupplierInfo(UblAccountingSupplierParty supplier);

    @Mapping(source = "r.businessName", target = "name")
    @Mapping(source = "r.businessOib", target = "oib")
    @Mapping(source = "supplier.street", target = "address")
    @Mapping(source = "supplier.city", target = "city")
    @Mapping(source = "supplier.postalZone", target = "postalCode")
    @Mapping(source = "supplier.contactName", target = "contactName")
    @Mapping(source = "supplier.contactOib", target = "contactOib")
    @Mapping(source = "supplier.email", target = "contactEmail")
    @Mapping(source = "supplier.phone", target = "contactPhone")
    SupplierInfo toSupplierInfo(SupplierProperties supplier, ReceiptDto r);

    @Mapping(source = "supplierName", target = "name")
    @Mapping(source = "supplierOib", target = "oib")
    @Mapping(source = "supplierAddress", target = "address")
    @Mapping(source = "supplierCity", target = "city")
    @Mapping(source = "supplierPostalCode", target = "postalCode")
    @Mapping(source = "supplierContactName", target = "contactName")
    @Mapping(source = "supplierContactOib", target = "contactOib")
    @Mapping(source = "supplierContactEmail", target = "contactEmail")
    @Mapping(source = "supplierContactPhone", target = "contactPhone")
    SupplierInfo toSupplierInfo(BillInfoEntity entity);

    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.description", target = "description")
    @Mapping(target = "quantity", expression = "java(new BigDecimal(line.getInvoicedQuantity().getValue().trim()))")
    @Mapping(target = "unitOfMeasure", expression = "java(ItemUnitOfMeasure.fromB2b(hr.bill.spring_bill.dto.eposlovanje.enums.UnitOfMeasure.fromInternationalCode(line.getInvoicedQuantity().getUnitCode())))")
    @Mapping(target = "baseAmount", expression = "java(new BigDecimal(line.getLineExtensionAmount().getValue().trim()))")
    @Mapping(target = "totalAmount", expression = "java(itemTotalAmount(line))")
    @Mapping(target = "vatRate", expression = "java(BillVatRate.fromVatCategory(VatCategory.fromId(line.getItem().getClassifiedTaxCategory().getId())))")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    BillItemInfo toItemInfo(UblInvoiceLine line);

    @Mapping(target = "unitOfMeasure", expression = "java(ItemUnitOfMeasure.fromB2c(item.unitOfMeasure()))")
    @Mapping(source = "totalPrice", target = "baseAmount")
    @Mapping(source = "totalPriceWithTax", target = "totalAmount")
    @Mapping(target = "vatRate", expression = "java(BillVatRate.fromTaxRate(item.taxRate()))")
    @Mapping(target = "currency", constant = "EUR")
    BillItemInfo toItemInfo(ReceiptItemDto item);

    @Mapping(target = "unitOfMeasure", expression = "java(ItemUnitOfMeasure.fromB2b(item.getUnitOfMeasure()))")
    @Mapping(target = "vatRate", expression = "java(BillVatRate.fromVatCategory(item.getVatCategory()))")
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    BillItemInfo toItemInfo(BillItemEntity item);

    default PaymentInfo toPaymentInfo(UblPaymentMeans pm, String invoiceDueDate) {
        return new PaymentInfo(
                BillPaymentMethod.fromPaymentMeans(PaymentMeans.fromCode(pm.getPaymentMeansCode())),
                pm.getPaymentDueDate() != null ? LocalDate.parse(pm.getPaymentDueDate()) : LocalDate.parse(invoiceDueDate),
                pm.getPayeeFinancialAccount().getId(),
                HrPaymentReferenceService.extractHrModel(pm.getPaymentId()),
                HrPaymentReferenceService.trimHrPrefix(pm.getPaymentId()),
                pm.getInstructionNote()
        );
    }

    @Mapping(target = "paymentMethod", expression = "java(BillPaymentMethod.fromPaymentMeans(entity.getPaymentMeans()))")
    @Mapping(source = "paymentDueDate", target = "dueDate")
    @Mapping(source = "paymentIban", target = "iban")
    @Mapping(source = "paymentModel", target = "model")
    @Mapping(source = "paymentReference", target = "reference")
    @Mapping(source = "paymentNote", target = "note")
    PaymentInfo toPaymentInfo(BillInfoEntity entity);

    @Mapping(target = "fiscalizedAt", expression = "java(r.fiscalizedAt() != null ? java.time.LocalDateTime.parse(r.fiscalizedAt()) : null)")
    FiscalInfo toFiscalInfo(ReceiptDto r);

    default PriceInfo toPriceInfo(UblLegalMonetaryTotal lmt, UblTaxTotal tt) {
        return new PriceInfo(
                new BigDecimal(lmt.getTaxExclusiveAmount().getValue().trim()),
                new BigDecimal(tt.getTaxAmount().getValue().trim()),
                new BigDecimal(lmt.getTaxInclusiveAmount().getValue().trim()),
                new BigDecimal(lmt.getPrepaidAmount().getValue().trim()),
                new BigDecimal(lmt.getPayableAmount().getValue().trim())
        );
    }

    @Mapping(source = "totalAmount", target = "vatExclusiveAmount")
    @Mapping(source = "taxAmount", target = "vatAmount")
    @Mapping(source = "grandTotal", target = "vatInclusiveAmount")
    @Mapping(target = "advanceAmount", ignore = true)
    @Mapping(source = "grandTotal", target = "totalAmount")
    PriceInfo toPriceInfo(ReceiptDto r);

    PriceInfo toPriceInfo(BillInfoEntity entity);

    default BigDecimal itemTotalAmount(UblInvoiceLine line) {
        BigDecimal base = new BigDecimal(line.getLineExtensionAmount().getValue().trim());
        BigDecimal rate = new BigDecimal(line.getItem().getClassifiedTaxCategory().getPercent().trim());
        return base.multiply(BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100))));
    }
}