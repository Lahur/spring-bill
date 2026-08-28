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
public class UblItem {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "Description")
    private String description;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "Name")
    private String name;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "CommodityClassification")
    private UblCommodityClassification commodityClassification;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "ClassifiedTaxCategory")
    private UblClassifiedTaxCategory classifiedTaxCategory;
}