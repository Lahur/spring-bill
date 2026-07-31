package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.ubl.UblNs;
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
public class UblAccountingSupplierParty {

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Party")
    private UblParty party;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "SellerContact")
    private UblSellerContact sellerContact;
}