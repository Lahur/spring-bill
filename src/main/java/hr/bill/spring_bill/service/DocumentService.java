package hr.bill.spring_bill.service;

import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dto.mail_bill.request.MailFile;
import hr.bill.spring_bill.dto.mail_bill.request.SendMailRequest;
import hr.bill.spring_bill.dto.web.SendBillReportItem;
import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.service.document.BillDocument;
import hr.bill.spring_bill.service.document.DocumentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    // hub-bill-app's PDF rendering is CPU-heavy; capping in-flight requests avoids
    // throttling it and leaves headroom for the rest of the node (see infra sizing notes).
    @Value("${bill.hub-render-concurrency:1}")
    private int hubRenderConcurrency;

    private final MailBillClient mailBillClient;

    private final RecipientService recipientService;

    private final DocumentStrategyFactory documentStrategyFactory;

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

        String title = LocalDateTime.now(CroatianTimeZone.ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<MailFile> files = sendBillReportsRequest.separated()
                ? individualFiles(pdfsDir, title)
                : List.of(mergedFile(pdfsDir, title));
        SendMailRequest sendMailRequest = SendMailRequest.builder()
                .subject(title)
                .recipientEmail(sendBillReportsRequest.email())
                .files(files)
                .build();
        mailBillClient.sendMail(sendMailRequest);
        recipientService.save(sendBillReportsRequest.email());
        log.info("Sent {} {} document attachment(s) to {}",
                files.size(), sendBillReportsRequest.separated() ? "individual" : "merged", sendBillReportsRequest.email());

        for (SendBillReportItem item : sendBillReportsRequest.reports()) {
            documentStrategyFactory.incrementSentCount(item.type(), item.id());
        }
    }

    // Merge every rendered PDF into one A4-normalized document.
    private MailFile mergedFile(Path pdfsDir, String title) {
        try (var pdfFiles = Files.list(pdfsDir)) {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            merger.setDestinationStream(mergedOutput);
            for (Path pdfFile : pdfFiles.sorted().toList()) {
                merger.addSource(pdfFile.toFile());
            }
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return MailFile.builder()
                    .fileName(title)
                    .fileContent(Base64.getEncoder().encodeToString(normalizeToA4(mergedOutput.toByteArray())))
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Attach every rendered PDF individually (A4-normalized) to a single mail,
    // named "<title>-<n>-<total>" (e.g. 2026-09-10-2-3).
    private List<MailFile> individualFiles(Path pdfsDir, String title) {
        try (var pdfFiles = Files.list(pdfsDir)) {
            List<Path> sorted = pdfFiles.sorted().toList();
            List<MailFile> files = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                byte[] normalized = normalizeToA4(Files.readAllBytes(sorted.get(i)));
                files.add(MailFile.builder()
                        .fileName(String.format("%s-%d-%d", title, i + 1, sorted.size()))
                        .fileContent(Base64.getEncoder().encodeToString(normalized))
                        .build());
            }
            return files;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Source documents are rendered at wildly different page sizes (A4 invoices,
    // narrow POS receipt rolls, ...). Re-lay every merged page onto an A4 sheet,
    // scaling the original content to fit while keeping its aspect ratio and never
    // enlarging it, so the mailed PDF is a single uniform format. Content is
    // anchored to the top-left corner, so small receipts sit there rather than
    // floating in the middle of the page.
    private byte[] normalizeToA4(byte[] merged) throws IOException {
        float a4Width = PDRectangle.A4.getWidth();
        float a4Height = PDRectangle.A4.getHeight();

        try (PDDocument source = Loader.loadPDF(merged);
             PDDocument target = new PDDocument()) {
            LayerUtility layerUtility = new LayerUtility(target);

            for (int i = 0; i < source.getNumberOfPages(); i++) {
                PDPage a4Page = new PDPage(PDRectangle.A4);
                target.addPage(a4Page);

                // importPageAsForm bakes the source page's rotation and crop box into
                // the form, so its BBox is the visible content size.
                PDFormXObject form = layerUtility.importPageAsForm(source, i);
                PDRectangle bbox = form.getBBox();
                float contentWidth = bbox.getWidth();
                float contentHeight = bbox.getHeight();

                float scale = Math.min(a4Width / contentWidth, a4Height / contentHeight);
                scale = Math.min(scale, 1f); // fit only, never upscale

                float scaledHeight = contentHeight * scale;

                // PDF origin is bottom-left; anchor top-left: x = 0, y = top of the sheet.
                AffineTransform transform = new AffineTransform();
                transform.translate(0f, a4Height - scaledHeight);
                transform.scale(scale, scale);
                transform.translate(-bbox.getLowerLeftX(), -bbox.getLowerLeftY());

                try (PDPageContentStream cs = new PDPageContentStream(target, a4Page, AppendMode.APPEND, true, true)) {
                    cs.saveGraphicsState();
                    cs.transform(new Matrix(transform));
                    cs.drawForm(form);
                    cs.restoreGraphicsState();
                }
            }

            ByteArrayOutputStream normalized = new ByteArrayOutputStream();
            target.save(normalized);
            return normalized.toByteArray();
        }
    }

    private void renderDocuments(List<SendBillReportItem> items, Path pdfsDir) {
        List<Callable<Void>> renderTasks = items.stream()
                .<Callable<Void>>map(item -> () -> {
                    renderDocument(item, pdfsDir);
                    return null;
                })
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Math.min(hubRenderConcurrency, renderTasks.size())));
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
        BillDocument billDocument = documentStrategyFactory.createDocument(item.type(), item.id());
        Files.write(pdfsDir.resolve(String.format("%d-%s%s", item.type().getOrder(), billDocument.filename(), ".pdf")),
                billDocument.content());
    }

}
