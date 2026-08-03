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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    // hub-bill-app's PDF rendering is CPU-heavy; capping in-flight requests to 2 avoids
    // throttling it and leaves headroom for the rest of the node (see infra sizing notes).
    private static final int HUB_RENDER_CONCURRENCY = 2;

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
        renderDocuments(sendBillReportsRequest.reports(), pdfsDir);

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

    private void renderDocuments(List<SendBillReportItem> items, Path pdfsDir) {
        List<Callable<Void>> renderTasks = items.stream()
                .<Callable<Void>>map(item -> () -> {
                    renderDocument(item, pdfsDir);
                    return null;
                })
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Math.min(HUB_RENDER_CONCURRENCY, renderTasks.size())));
        try {
            List<Future<Void>> futures = executor.invokeAll(renderTasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioe) throw new UncheckedIOException(ioe);
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } finally {
            executor.shutdown();
        }
    }

    private void renderDocument(SendBillReportItem item, Path pdfsDir) throws IOException {
        BillDocument billDocument = billStrategyFactory.createDocument(item.type(), item.id());
        Files.write(pdfsDir.resolve(String.format("%d-%s%s", item.type().getOrder(), billDocument.filename(), ".pdf")),
                billDocument.content());
    }

}
