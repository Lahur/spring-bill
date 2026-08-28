package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.bill_pdf.common.*;
import hr.bill.spring_bill.dto.bill_pdf.request.BillWithDetailsRequest;
import hr.bill.spring_bill.dto.bill_pdf.request.IncomingInvoiceRequest;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import hr.bill.spring_bill.service.NumberToWordsService;
import hr.bill.spring_bill.xml.ubl.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
        imports = {NumberToWordsService.class, VatCategory.class, LocalDate.class, ChronoUnit.class})
public interface UblInvoiceMapper {

    // -------------------------------------------------------------------------
    // BillWithDetailsRequest
    // -------------------------------------------------------------------------

    @Mapping(target = "recipientName", source = "ublInvoice.accountingCustomerParty.party.partyName.name")
    @Mapping(target = "recipientAddress", source = "ublInvoice.accountingCustomerParty.party.postalAddress.streetName")
    @Mapping(target = "recipientPost", source = "ublInvoice.accountingCustomerParty.party.postalAddress.postalZone")
    @Mapping(target = "recipientCity", source = "ublInvoice.accountingCustomerParty.party.postalAddress.cityName")
    @Mapping(target = "recipientOib", source = "ublInvoice.accountingCustomerParty.party.partyLegalEntity.companyId")
    @Mapping(target = "billNumber", source = "ublInvoice.id")
    @Mapping(target = "billDate", expression = "java(formatDate(ublInvoice.getIssueDate()))")
    @Mapping(target = "billTime", expression = "java(formatTime(ublInvoice.getIssueTime()))")
    @Mapping(target = "billProjectDescription", expression = "java(ublInvoice.getInvoiceLines().getFirst().getItem().getDescription())")
    @Mapping(target = "billProjectName", expression = "java(ublInvoice.getInvoiceLines().getFirst().getItem().getName())")
    @Mapping(target = "billBasePrice", expression = "java(formatAmount(ublInvoice.getLegalMonetaryTotal().getTaxExclusiveAmount()))")
    @Mapping(target = "billPdvPrice", expression = "java(formatAmount(ublInvoice.getTaxTotal().getTaxAmount()))")
    @Mapping(target = "billTotalPrice", expression = "java(formatAmount(ublInvoice.getLegalMonetaryTotal().getTaxInclusiveAmount()))")
    @Mapping(target = "billTotalText", expression = "java(NumberToWordsService.asWords(new java.math.BigDecimal(ublInvoice.getLegalMonetaryTotal().getTaxInclusiveAmount().getValue())))")
    @Mapping(target = "billReverseCharge", expression = "java(VatCategory.fromId(ublInvoice.getTaxTotal().getTaxSubtotals().getFirst().getTaxCategory().getId()) == VatCategory.ReverseCharge)")
    @Mapping(target = "paymentDays", expression = "java(ChronoUnit.DAYS.between(LocalDate.parse(ublInvoice.getDueDate()), LocalDate.parse(ublInvoice.getIssueDate())))")
    @Mapping(target = "zki", ignore = true)
    @Mapping(target = "jir", ignore = true)
    @Mapping(target = "details", source = "ublInvoice")
    BillWithDetailsRequest toBillWithDetailsRequest(UblInvoice ublInvoice, String pdf417Image);

    default String formatAmount(UblAmount amount) {
        return amount == null ? null : amount.getValue().replace('.', ',');
    }

    @Named("formatDate")
    default String formatDate(String isoDate) {
        return isoDate == null ? null : LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
    }

    @Named("formatTime")
    default String formatTime(String isoTime) {
        return isoTime == null ? null : LocalTime.parse(isoTime, DateTimeFormatter.ofPattern("HH:mm:ss")).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // -------------------------------------------------------------------------
    // BillDetailsDto
    // -------------------------------------------------------------------------

    @Mapping(source = "id", target = "billNumber")
    @Mapping(source = "issueDate", target = "billDate")
    @Mapping(source = "documentCurrencyCode", target = "currencyCode")
    @Mapping(source = "invoicePeriod.startDate", target = "periodStart")
    @Mapping(source = "invoicePeriod.endDate", target = "periodEnd")
    @Mapping(source = "orderReference.id", target = "orderReferenceId")
    @Mapping(target = "invoiceNote", expression = "java(firstNote(inv))")
    @Mapping(source = "accountingSupplierParty", target = "supplier")
    @Mapping(source = "accountingCustomerParty", target = "customer")
    @Mapping(source = "paymentMeans", target = "paymentMeans")
    @Mapping(source = "invoiceLines", target = "lines")
    @Mapping(target = "taxSubtotals", expression = "java(toTaxSubtotalList(inv.getTaxTotal()))")
    @Mapping(target = "taxTotalAmount", expression = "java(taxTotalAmount(inv.getTaxTotal()))")
    @Mapping(source = "legalMonetaryTotal", target = "monetary")
    BillDetailsDto toBillDetailsDto(UblInvoice inv);

    @Mapping(source = "party.partyName.name", target = "name")
    @Mapping(source = "party.endpointId.value", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "street")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalZone")
    @Mapping(target = "countryCode", expression = "java(countryCode(outer.getParty()))")
    @Mapping(source = "party.contact.name", target = "contactName")
    @Mapping(source = "sellerContact.id", target = "contactOib")
    @Mapping(source = "party.contact.telephone", target = "phone")
    @Mapping(source = "party.contact.electronicMail", target = "email")
    OutgoingSupplierDto toOutgoingSupplier(UblAccountingSupplierParty outer);

    @Mapping(source = "party.partyName.name", target = "name")
    @Mapping(source = "party.endpointId.value", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "street")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalZone")
    @Mapping(target = "countryCode", expression = "java(countryCode(outer.getParty()))")
    OutgoingCustomerDto toOutgoingCustomer(UblAccountingCustomerParty outer);

    @Mapping(source = "paymentMeansCode", target = "code")
    @Mapping(source = "paymentDueDate", target = "dueDate")
    @Mapping(source = "paymentChannelCode", target = "channelCode")
    @Mapping(source = "payeeFinancialAccount.id", target = "iban")
    @Mapping(source = "payeeFinancialAccount.currencyCode", target = "accountCurrencyCode")
    OutgoingPaymentMeansDto toOutgoingPaymentMeans(UblPaymentMeans pm);

    @Mapping(source = "lineExtensionAmount.value", target = "lineExtensionAmount")
    @Mapping(source = "taxExclusiveAmount.value", target = "taxExclusiveAmount")
    @Mapping(source = "taxInclusiveAmount.value", target = "taxInclusiveAmount")
    @Mapping(source = "prepaidAmount.value", target = "prepaidAmount")
    @Mapping(source = "payableAmount.value", target = "payableAmount")
    MonetaryTotalDto toMonetaryTotal(UblLegalMonetaryTotal lmt);

    // -------------------------------------------------------------------------
    // IncomingInvoiceRequest
    // -------------------------------------------------------------------------

    @Mapping(source = "id", target = "invoiceId")
    @Mapping(source = "documentCurrencyCode", target = "currencyCode")
    @Mapping(target = "deliveryDate", ignore = true)
    @Mapping(source = "accountingSupplierParty", target = "supplier")
    @Mapping(source = "accountingCustomerParty", target = "customer")
    @Mapping(source = "paymentMeans", target = "paymentMeans")
    @Mapping(target = "taxTotal", expression = "java(toIncomingTaxTotal(inv.getTaxTotal()))")
    @Mapping(source = "legalMonetaryTotal", target = "monetaryTotal")
    @Mapping(source = "invoiceLines", target = "lines")
    IncomingInvoiceRequest toIncomingInvoiceRequest(UblInvoice inv);

    @Mapping(target = "name", expression = "java(legalName(outer.getParty()))")
    @Mapping(source = "party.partyLegalEntity.companyId", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "street")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalZone")
    @Mapping(target = "countryCode", expression = "java(countryCode(outer.getParty()))")
    @Mapping(source = "party.contact.name", target = "contactName")
    @Mapping(source = "party.contact.electronicMail", target = "email")
    IncomingSupplierDto toIncomingSupplier(UblAccountingSupplierParty outer);

    @Mapping(target = "name", expression = "java(legalName(outer.getParty()))")
    @Mapping(source = "party.partyLegalEntity.companyId", target = "oib")
    @Mapping(source = "party.postalAddress.streetName", target = "street")
    @Mapping(source = "party.postalAddress.cityName", target = "city")
    @Mapping(source = "party.postalAddress.postalZone", target = "postalZone")
    @Mapping(target = "countryCode", expression = "java(countryCode(outer.getParty()))")
    @Mapping(source = "party.contact.name", target = "contactName")
    @Mapping(source = "party.contact.electronicMail", target = "contactEmail")
    IncomingCustomerDto toIncomingCustomer(UblAccountingCustomerParty outer);

    @Mapping(source = "payeeFinancialAccount.id", target = "iban")
    @Mapping(source = "payeeFinancialAccount.currencyCode", target = "accountCurrencyCode")
    IncomingPaymentMeansDto toIncomingPaymentMeans(UblPaymentMeans pm);

    // -------------------------------------------------------------------------
    // Shared sub-mappers
    // -------------------------------------------------------------------------

    @Mapping(source = "taxCategory.id", target = "categoryId")
    @Mapping(source = "taxCategory.percent", target = "percent")
    @Mapping(source = "taxableAmount.value", target = "taxableAmount")
    @Mapping(source = "taxAmount.value", target = "taxAmount")
    @Mapping(source = "taxCategory.taxExemptionReason", target = "taxExemptionReason")
    TaxSubtotalDto toTaxSubtotal(UblTaxSubtotal sub);

    @Mapping(target = "id", expression = "java(lineId(line))")
    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.description", target = "description")
    @Mapping(target = "classificationCode", expression = "java(classificationCode(line))")
    @Mapping(source = "invoicedQuantity.value", target = "quantity")
    @Mapping(source = "invoicedQuantity.unitCode", target = "unitCode")
    @Mapping(source = "price.priceAmount.value", target = "unitPrice")
    @Mapping(source = "lineExtensionAmount.value", target = "lineExtensionAmount")
    @Mapping(target = "vatCategory", expression = "java(vatCategory(line))")
    InvoiceLineDto toInvoiceLine(UblInvoiceLine line);

    // -------------------------------------------------------------------------
    // Default helpers
    // -------------------------------------------------------------------------

    default String firstNote(UblInvoice inv) {
        return inv.getNotes() != null && !inv.getNotes().isEmpty() ? inv.getNotes().get(0) : "";
    }

    default String countryCode(UblParty p) {
        if (p == null || p.getPostalAddress() == null || p.getPostalAddress().getCountry() == null) return "";
        String code = p.getPostalAddress().getCountry().getIdentificationCode();
        return code != null ? code : "";
    }

    default String legalName(UblParty p) {
        if (p == null) return "";
        if (p.getPartyName() != null && p.getPartyName().getName() != null && !p.getPartyName().getName().isBlank()) {
            return p.getPartyName().getName();
        }
        return p.getPartyLegalEntity() != null && p.getPartyLegalEntity().getRegistrationName() != null
                ? p.getPartyLegalEntity().getRegistrationName() : "";
    }

    default List<TaxSubtotalDto> toTaxSubtotalList(UblTaxTotal tt) {
        if (tt == null || tt.getTaxSubtotals() == null) return List.of();
        return tt.getTaxSubtotals().stream().map(this::toTaxSubtotal).toList();
    }

    default String taxTotalAmount(UblTaxTotal tt) {
        if (tt == null || tt.getTaxAmount() == null) return "0.00";
        String v = tt.getTaxAmount().getValue();
        return v != null ? v : "0.00";
    }

    default IncomingTaxTotalDto toIncomingTaxTotal(UblTaxTotal tt) {
        return new IncomingTaxTotalDto(taxTotalAmount(tt), toTaxSubtotalList(tt));
    }

    default Integer lineId(UblInvoiceLine line) {
        try {
            return line.getId() != null ? Integer.parseInt(line.getId().trim()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    default String classificationCode(UblInvoiceLine line) {
        UblItem item = line.getItem();
        UblCommodityClassification cc = item != null ? item.getCommodityClassification() : null;
        if (cc == null || cc.getItemClassificationCode() == null) return "";
        String v = cc.getItemClassificationCode().getValue();
        return v != null ? v : "";
    }

    default String vatCategory(UblInvoiceLine line) {
        UblItem item = line.getItem();
        UblClassifiedTaxCategory tc = item != null ? item.getClassifiedTaxCategory() : null;
        if (tc == null || tc.getId() == null) return "";
        return tc.getId();
    }

}