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
public class UblInvoicePeriod {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "StartDate")
    private String startDate;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "EndDate")
    private String endDate;
}