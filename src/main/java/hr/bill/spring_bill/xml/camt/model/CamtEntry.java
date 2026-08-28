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
public class CamtEntry {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Amt")
    private CamtAmount amt;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "CdtDbtInd")
    private String cdtDbtInd;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "RvslInd")
    private Boolean rvslInd;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Sts")
    private CamtEntryStatus sts;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "BookgDt")
    private CamtDateTimeChoice bookgDt;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "ValDt")
    private CamtDateChoice valDt;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "AcctSvcrRef")
    private String acctSvcrRef;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "BkTxCd")
    private CamtBankTransactionCode bkTxCd;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "NtryDtls")
    private CamtEntryDetails ntryDtls;
}
