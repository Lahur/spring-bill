package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblAmount {

    @JacksonXmlProperty(isAttribute = true, localName = "currencyID")
    private String currencyId;

    @JacksonXmlText
    private String value;
}
