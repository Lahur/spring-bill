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
public class CamtAccount {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Id")
    private CamtAccountIdentification id;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Ccy")
    private String ccy;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Nm")
    private String nm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Ownr")
    private CamtAccountOwner ownr;
}
