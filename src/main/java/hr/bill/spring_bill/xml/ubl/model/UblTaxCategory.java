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
public class UblTaxCategory {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ID")
    private String id;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "Percent")
    private String percent;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "TaxExemptionReason")
    private String taxExemptionReason;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "TaxScheme")
    private UblTaxScheme taxScheme;
}
