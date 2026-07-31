package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.ubl.UblNs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblTaxTotal {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "TaxAmount")
    private UblAmount taxAmount;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "TaxSubtotal")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<UblTaxSubtotal> taxSubtotals;
}