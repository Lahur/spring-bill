package hr.bill.spring_bill.xml.camt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import hr.bill.spring_bill.xml.camt.CamtNs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JacksonXmlRootElement(namespace = CamtNs.CAMT_053, localName = "Document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamtDocument {

    @JacksonXmlProperty(namespace = CamtNs.CAMT_053, localName = "BkToCstmrStmt")
    private CamtBankToCustomerStatement bkToCstmrStmt;
}