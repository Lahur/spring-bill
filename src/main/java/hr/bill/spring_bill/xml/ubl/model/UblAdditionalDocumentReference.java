package hr.bill.spring_bill.xml.ubl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
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
public class UblAdditionalDocumentReference {

    @JacksonXmlProperty(namespace = UblNs.CBC, localName = "ID")
    private String id;

    @JacksonXmlProperty(namespace = UblNs.CAC, localName = "Attachment")
    private Attachment attachment;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {

        @JacksonXmlProperty(namespace = UblNs.CBC, localName = "EmbeddedDocumentBinaryObject")
        private EmbeddedDocumentBinaryObject embeddedDocumentBinaryObject;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmbeddedDocumentBinaryObject {

            @JacksonXmlProperty(isAttribute = true, localName = "mimeCode")
            private String mimeCode;

            @JacksonXmlProperty(isAttribute = true, localName = "filename")
            private String filename;

            @JacksonXmlText
            private String value;
        }
    }
}