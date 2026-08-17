package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.config.SupplierProperties;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.PaymentInfo;
import hr.bill.spring_bill.dto.eposlovanje.eposlovanje_util.common.PaymentParty;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptDto;
import hr.bill.spring_bill.service.HrPaymentReferenceService;
import hr.bill.spring_bill.xml.ubl.model.UblInvoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = HrPaymentReferenceService.class)
public interface PaymentInfoMapper {

    @Mapping(target = "payeeName", source = "supplier.name")
    @Mapping(target = "payeeAddress", source = "supplier.street")
    @Mapping(target = "payeeCity", source = "supplier.city")
    @Mapping(target = "payeeZip", source = "supplier.postalZone")
    @Mapping(target = "payeeIBAN", source = "supplier.iban")
    @Mapping(target = "payerName", source = "ublInvoice.accountingCustomerParty.party.partyName.name")
    @Mapping(target = "payerAddress", source = "ublInvoice.accountingCustomerParty.party.postalAddress.streetName")
    @Mapping(target = "payerCity", source = "ublInvoice.accountingCustomerParty.party.postalAddress.cityName")
    @Mapping(target = "payerZip", source = "ublInvoice.accountingCustomerParty.party.postalAddress.postalZone")
    @Mapping(target = "currency", source = "ublInvoice.documentCurrencyCode")
    @Mapping(target = "amount", expression = "java(Math.abs(new java.math.BigDecimal(ublInvoice.getLegalMonetaryTotal().getTaxInclusiveAmount().getValue()).doubleValue()))")
    @Mapping(target = "model", expression = "java(HrPaymentReferenceService.extractHrModel(ublInvoice.getPaymentMeans().getPaymentId()))")
    @Mapping(target = "referenceNumber", expression = "java(HrPaymentReferenceService.trimHrPrefix(ublInvoice.getPaymentMeans().getPaymentId()))")
    @Mapping(target = "description", expression = "java(\"račun \" + ublInvoice.getId())")
    PaymentInfo toPaymentInfo(SupplierProperties supplier, UblInvoice ublInvoice);

    @Mapping(target = "payeeName", source = "ublInvoice.accountingSupplierParty.party.partyName.name")
    @Mapping(target = "payeeAddress", source = "ublInvoice.accountingSupplierParty.party.postalAddress.streetName")
    @Mapping(target = "payeeCity", source = "ublInvoice.accountingSupplierParty.party.postalAddress.cityName")
    @Mapping(target = "payeeZip", source = "ublInvoice.accountingSupplierParty.party.postalAddress.postalZone")
    @Mapping(target = "payeeIBAN", source = "ublInvoice.paymentMeans.payeeFinancialAccount.id")
    @Mapping(target = "payerName", source = "supplier.name")
    @Mapping(target = "payerAddress", source = "supplier.street")
    @Mapping(target = "payerCity", source = "supplier.city")
    @Mapping(target = "payerZip", source = "supplier.postalZone")
    @Mapping(target = "currency", source = "ublInvoice.documentCurrencyCode")
    @Mapping(target = "amount", expression = "java(Math.abs(new java.math.BigDecimal(ublInvoice.getLegalMonetaryTotal().getTaxInclusiveAmount().getValue()).doubleValue()))")
    @Mapping(target = "model", expression = "java(HrPaymentReferenceService.extractHrModel(ublInvoice.getPaymentMeans().getPaymentId()))")
    @Mapping(target = "referenceNumber", expression = "java(HrPaymentReferenceService.trimHrPrefix(ublInvoice.getPaymentMeans().getPaymentId()))")
    @Mapping(target = "description", expression = "java(\"račun \" + ublInvoice.getId())")
    PaymentInfo toIngoingPaymentInfo(SupplierProperties supplier, UblInvoice ublInvoice);

    @Mapping(target = "payeeName", source = "supplier.name")
    @Mapping(target = "payeeAddress", source = "supplier.street")
    @Mapping(target = "payeeCity", source = "supplier.city")
    @Mapping(target = "payeeZip", source = "supplier.postalZone")
    @Mapping(target = "payeeIBAN", source = "supplier.iban")
    @Mapping(target = "payerName", source = "payer.name")
    @Mapping(target = "payerAddress", source = "payer.address")
    @Mapping(target = "payerCity", source = "payer.city")
    @Mapping(target = "payerZip", source = "payer.postalZone")
    @Mapping(target = "amount", expression = "java(Math.abs(amount))")
    PaymentInfo toPaymentInfo(
            SupplierProperties supplier,
            PaymentParty payer,
            String currency,
            Double amount,
            String model,
            String referenceNumber,
            String description
    );

    @Mapping(target = "payeeName", source = "supplier.name")
    @Mapping(target = "payeeAddress", source = "supplier.street")
    @Mapping(target = "payeeCity", source = "supplier.city")
    @Mapping(target = "payeeZip", source = "supplier.postalZone")
    @Mapping(target = "payeeIBAN", source = "supplier.iban")
    @Mapping(target = "payerName", source = "receiptDto.buyerName")
    @Mapping(target = "payerAddress", source = "receiptDto.buyerAddress")
    @Mapping(target = "payerCity", source = "receiptDto.buyerCity")
    @Mapping(target = "payerZip", source = "receiptDto.buyerPostalCode")
    @Mapping(target = "currency", constant = "EUR")
    @Mapping(target = "amount", expression = "java(Math.abs(receiptDto.grandTotal()))")
    @Mapping(target = "referenceNumber", expression = "java(receiptDto.formattedReceiptNumber().replace('/', '-').replace('\\\\', '-'))")
    @Mapping(target = "description", expression = "java(\"račun \" + receiptDto.formattedReceiptNumber())")
    PaymentInfo toPaymentInfo(SupplierProperties supplier, ReceiptDto receiptDto, String model);
}