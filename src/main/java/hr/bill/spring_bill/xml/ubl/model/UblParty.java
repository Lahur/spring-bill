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
public class UblParty {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "EndpointID")
    private UblEndpointId endpointId;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PartyName")
    private UblPartyName partyName;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PostalAddress")
    private UblPostalAddress postalAddress;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PartyTaxScheme")
    private UblPartyTaxScheme partyTaxScheme;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PartyLegalEntity")
    private UblPartyLegalEntity partyLegalEntity;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Contact")
    private UblContact contact;
}