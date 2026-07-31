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
public class UblPaymentMeans {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "PaymentMeansCode")
    private String paymentMeansCode;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "PaymentDueDate")
    private String paymentDueDate;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "PaymentChannelCode")
    private String paymentChannelCode;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "InstructionID")
    private String instructionId;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "InstructionNote")
    private String instructionNote;

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "PaymentID")
    private String paymentId;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "PayeeFinancialAccount")
    private Account payeeFinancialAccount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {

        @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ID")
        private String id;

        @JacksonXmlProperty(namespace = UblNs.CBC, localName = "CurrencyCode")
        private String currencyCode;
    }
}