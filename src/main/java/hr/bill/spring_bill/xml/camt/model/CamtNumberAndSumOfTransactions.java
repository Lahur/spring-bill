package hr.bill.spring_bill.xml.camt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.camt.CamtNs;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamtNumberAndSumOfTransactions {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "NbOfNtries")
    private String nbOfNtries;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Sum")
    private String sum;
}
