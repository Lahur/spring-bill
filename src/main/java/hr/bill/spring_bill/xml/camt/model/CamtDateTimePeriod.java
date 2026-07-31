package hr.bill.spring_bill.xml.camt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.camt.CamtNs;
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
public class CamtDateTimePeriod {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "FrDtTm")
    private String frDtTm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "ToDtTm")
    private String toDtTm;
}
