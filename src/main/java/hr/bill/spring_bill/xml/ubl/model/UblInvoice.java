package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import hr.bill.spring_bill.xml.ubl.UblNs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JacksonXmlRootElement(namespace = UblNs.INVOICE, localName = "Invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblInvoice {

    @JacksonXmlProperty(namespace = UblNs.EXT, localName = "UBLExtensions")
    private UblExtensions ublExtensions;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "CustomizationID")
    private String customizationId;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ProfileID")
    private String profileId;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ID")
    private String id;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "CopyIndicator")
    private Boolean copyIndicator;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "IssueDate")
    private String issueDate;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "IssueTime")
    private String issueTime;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "DueDate")
    private String dueDate;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "InvoiceTypeCode")
    private String invoiceTypeCode;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "Note")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<String> notes;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "DocumentCurrencyCode")
    private String documentCurrencyCode;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "InvoicePeriod")
    private UblInvoicePeriod invoicePeriod;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "OrderReference")
    private UblOrderReference orderReference;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "AdditionalDocumentReference")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<UblAdditionalDocumentReference> additionalDocumentReferences;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "AccountingSupplierParty")
    private UblAccountingSupplierParty accountingSupplierParty;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "AccountingCustomerParty")
    private UblAccountingCustomerParty accountingCustomerParty;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PaymentMeans")
    private UblPaymentMeans paymentMeans;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "TaxTotal")
    private UblTaxTotal taxTotal;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "LegalMonetaryTotal")
    private UblLegalMonetaryTotal legalMonetaryTotal;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "InvoiceLine")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<UblInvoiceLine> invoiceLines;
}