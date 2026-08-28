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
public class CamtTransactionParties {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Dbtr")
    private CamtPartyWrapper dbtr;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "DbtrAcct")
    private CamtCashAccount dbtrAcct;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "UltmtDbtr")
    private CamtPartyWrapper ultmtDbtr;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Cdtr")
    private CamtPartyWrapper cdtr;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "CdtrAcct")
    private CamtCashAccount cdtrAcct;
}
