package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dto.mail_bill.request.SendMailRequest;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MailBillClient mailBillClient;

    private final BillStrategyFactory billStrategyFactory;

    public void generateAndSendDocuments(SendBillReportsRequest sendBillReportsRequest) {
        log.info("Generating and sending {} document(s) to {}",
                sendBillReportsRequest.reports().size(), sendBillReportsRequest.email());
        Path tempDir;
        Path pdfsDir;
        try {
            tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
            pdfsDir = Files.createDirectory(tempDir.resolve("pdfs"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (SendBillReportItem item : sendBillReportsRequest.reports()) {
            BillDocument billDocument = billStrategyFactory.createDocument(item.type(), item.id());
            try {
                Files.write(pdfsDir.resolve(String.format("%d-%s%s", item.type().getOrder(), billDocument.filename(), ".pdf")),
                        billDocument.content());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        String fileContent;
        try (var pdfFiles = Files.list(pdfsDir)) {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            merger.setDestinationStream(mergedOutput);
            for (Path pdfFile : pdfFiles.sorted().toList()) {
                merger.addSource(pdfFile.toFile());
            }
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            fileContent = Base64.getEncoder().encodeToString(mergedOutput.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String title = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SendMailRequest sendMailRequest = SendMailRequest.builder()
                .fileName(title)
                .subject(title)
                .recipientEmail(sendBillReportsRequest.email())
                .fileContent(fileContent)
                .build();
        mailBillClient.sendMail(sendMailRequest);
        log.info("Sent merged document mail to {}", sendBillReportsRequest.email());

        for (SendBillReportItem item : sendBillReportsRequest.reports()) {
            billStrategyFactory.incrementSentCount(item.type(), item.id());
        }
    }




}
