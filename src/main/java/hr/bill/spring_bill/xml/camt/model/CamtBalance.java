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
public class CamtBalance {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Tp")
    private CamtTypeChoice tp;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Amt")
    private CamtAmount amt;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "CdtDbtInd")
    private String cdtDbtInd;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Dt")
    private CamtDateChoice dt;
}
