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
public class CamtParty {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Nm")
    private String nm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "PstlAdr")
    private CamtPostalAddress pstlAdr;
}
