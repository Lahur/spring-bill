package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.ubl.UblNs;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblTaxSubtotal {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "TaxableAmount")
    private UblAmount taxableAmount;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "TaxAmount")
    private UblAmount taxAmount;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "TaxCategory")
    private UblTaxCategory taxCategory;
}