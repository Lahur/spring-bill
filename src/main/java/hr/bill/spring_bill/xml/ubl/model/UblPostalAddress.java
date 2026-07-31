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
public class UblPostalAddress {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "StreetName")
    private String streetName;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "CityName")
    private String cityName;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "PostalZone")
    private String postalZone;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Country")
    private Country country;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country {

        @JacksonXmlProperty(namespace = UblNs.CBC, localName = "IdentificationCode")
        private String identificationCode;
    }
}