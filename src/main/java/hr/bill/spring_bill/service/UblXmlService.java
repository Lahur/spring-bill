package hr.bill.spring_bill.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hr.bill.spring_bill.xml.ubl.UblNs;
import hr.bill.spring_bill.xml.ubl.model.UblInvoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
public class UblXmlService {

    private final XmlMapper xmlMapper;

    public String generateXml(UblInvoice invoice) {
        try {
            StringWriter sw = new StringWriter();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(sw);
            streamWriter.setDefaultNamespace(UblNs.INVOICE);
            streamWriter.setPrefix("cac", UblNs.CAC);
            streamWriter.setPrefix("cbc", UblNs.CBC);
            streamWriter.setPrefix("ext", UblNs.EXT);
            streamWriter.setPrefix("sig", UblNs.SIG);
            streamWriter.setPrefix("sac", UblNs.SAC);
            try (var generator = xmlMapper.getFactory().createGenerator(streamWriter)) {
                generator.writeObject(invoice);
            }
            streamWriter.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate UBL XML", e);
        }
    }

    public UblInvoice parse(String xml) {
        try {
            return xmlMapper.readValue(xml, UblInvoice.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse UBL XML", e);
        }
    }

}