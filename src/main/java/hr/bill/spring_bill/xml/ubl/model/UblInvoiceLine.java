package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.ubl.UblNs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblInvoiceLine {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ID")
    private String id;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "InvoicedQuantity")
    private UblQuantity invoicedQuantity;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "LineExtensionAmount")
    private UblAmount lineExtensionAmount;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Item")
    private UblItem item;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Price")
    private UblPrice price;
}