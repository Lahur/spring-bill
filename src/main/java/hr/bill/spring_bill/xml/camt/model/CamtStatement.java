package hr.bill.spring_bill.xml.camt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.camt.CamtNs;
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
public class CamtStatement {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Id")
    private String id;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "LglSeqNb")
    private String lglSeqNb;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "CreDtTm")
    private String creDtTm;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "FrToDt")
    private CamtDateTimePeriod frToDt;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "RptgSrc")
    private CamtReportingSource rptgSrc;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Acct")
    private CamtAccount acct;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Bal")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CamtBalance> bal;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "TxsSummry")
    private CamtTotalTransactions txsSummry;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Ntry")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<CamtEntry> ntry;
}
