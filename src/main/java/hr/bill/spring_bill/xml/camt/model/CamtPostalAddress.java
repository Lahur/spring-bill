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
public class CamtPostalAddress {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "StrtNm")
    private String strtNm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "BldgNb")
    private String bldgNb;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "PstCd")
    private String pstCd;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "TwnNm")
    private String twnNm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "CtrySubDvsn")
    private String ctrySubDvsn;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Ctry")
    private String ctry;
}
