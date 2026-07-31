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
public class CamtTransactionReferences {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "MsgId")
    private String msgId;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "AcctSvcrRef")
    private String acctSvcrRef;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "InstrId")
    private String instrId;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "EndToEndId")
    private String endToEndId;
}
