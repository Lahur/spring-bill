package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import hr.bill.spring_bill.xml.ubl.UblNs;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UblExtensions {

    @JacksonXmlProperty(namespace = UblNs.EXT, localName = "UBLExtension")
    private Extension ublExtension;

    public static UblExtensions empty() {
        return UblExtensions.builder()
                .ublExtension(Extension.builder()
                        .extensionContent(Extension.Content.builder()
                                .documentSignatures(Extension.Content.Signatures.builder()
                                        .signatureInformation(new Extension.Content.Signatures.SignatureInfo())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extension {

        @JacksonXmlProperty(namespace = UblNs.EXT, localName = "ExtensionContent")
        private Content extensionContent;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Content {

            @JacksonXmlProperty(namespace = UblNs.SIG, localName = "UBLDocumentSignatures")
            private Signatures documentSignatures;

            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Signatures {

                @JacksonXmlProperty(namespace = UblNs.SAC, localName = "SignatureInformation")
                private SignatureInfo signatureInformation;

                @Getter
                @Setter
                @NoArgsConstructor
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class SignatureInfo {
                }
            }
        }
    }
}