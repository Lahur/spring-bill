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
public class CamtEntryTransaction {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "Refs")
    private CamtTransactionReferences refs;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "AmtDtls")
    private CamtAmountAndCurrencyExchangeDetails amtDtls;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "RltdPties")
    private CamtTransactionParties rltdPties;

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "RmtInf")
    private CamtRemittanceInformation rmtInf;
}
