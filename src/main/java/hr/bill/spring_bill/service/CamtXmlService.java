package hr.bill.spring_bill.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hr.bill.spring_bill.xml.camt.CamtNs;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamtXmlService {

    private final XmlMapper xmlMapper;

    public String generateXml(CamtDocument document) {
        log.debug("Generating CAMT XML");
        try {
            StringWriter sw = new StringWriter();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(sw);
            streamWriter.setDefaultNamespace(CamtNs.CAMT_053);
            try (var generator = xmlMapper.getFactory().createGenerator(streamWriter)) {
                generator.writeObject(document);
            }
            streamWriter.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CAMT XML", e);
        }
    }

    public CamtDocument parse(String xml) {
        log.debug("Parsing CAMT XML ({} chars)", xml.length());
        try {
            return xmlMapper.readValue(xml, CamtDocument.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CAMT XML", e);
        }
    }

}
