package hr.bill.spring_bill.service;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.mail_bill.request.SendMailRequest;
import hr.bill.spring_bill.dto.web.pos.PosTransactionGenerateAndSendRequest;
import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.PosTransactionMapper;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.PosTransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosTransactionService {

    private final PosTransactionRepository posTransactionRepository;

    private final PosTransactionMapper posTransactionMapper;

    private final BillPdfClient billPdfClient;

    private final MailBillClient mailBillClient;

    @Value("${bill.path.pos}")
    private String posPath;

    public List<PosTransactionResponse> findAll() {
        log.debug("Fetching all POS transactions");
        List<PosTransactionResponse> result = posTransactionMapper.toPosTransactionResponseList(
                posTransactionRepository.findAllByOrderByBankTransaction_TransactionDateDesc());
        log.debug("Found {} POS transactions", result.size());
        return result;
    }

    public PosTransactionResponse upload(UUID id, MultipartFile file) {
        log.info("Uploading PDF for POS transaction {}", id);
        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Uploaded file must be a non-empty PDF");
        }
        PosTransactionEntity entity = posTransactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("POS transaction not found for id: " + id));
        entity.setBillPath(storeFile(file, id).toString());
        PosTransactionEntity saved = posTransactionRepository.save(entity);
        log.info("Uploaded PDF for POS transaction {}", id);
        return posTransactionMapper.toPosTransactionResponse(saved);
    }

    public void generateAndSendReports(PosTransactionGenerateAndSendRequest request) {
        log.info("Generating and sending {} POS transaction report(s) to {}",
                request.ids().size(), request.email());
        List<PosTransactionEntity> entities = posTransactionRepository.findAllById(request.ids());
        if (entities.size() != request.ids().size()) {
            List<UUID> foundIds = entities.stream().map(PosTransactionEntity::getId).toList();
            List<UUID> missingIds = request.ids().stream().filter(id -> !foundIds.contains(id)).toList();
            throw new NotFoundException("POS transaction(s) not found for id(s): " + missingIds);
        }

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(UUID.randomUUID().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<Path> pdfPaths = new ArrayList<>();
        for (PosTransactionEntity entity : entities) {
            byte[] content = billPdfClient.renderPosTransaction(posTransactionMapper.toPosTransactionRequest(entity));
            Path reportPath = tempDir.resolve(entity.getId() + "-report.pdf");
            try {
                Files.write(reportPath, content);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            pdfPaths.add(reportPath);
            if (entity.getBillPath() != null) {
                pdfPaths.add(Path.of(entity.getBillPath()));
            }
        }

        String fileContent;
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            merger.setDestinationStream(mergedOutput);
            for (Path pdfPath : pdfPaths) {
                merger.addSource(pdfPath.toFile());
            }
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            fileContent = Base64.getEncoder().encodeToString(mergedOutput.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String title = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SendMailRequest sendMailRequest = SendMailRequest.builder()
                .fileName("pos-" + title)
                .subject(title)
                .recipientEmail(request.email())
                .fileContent(fileContent)
                .build();
        mailBillClient.sendMail(sendMailRequest);
        log.info("Sent merged POS transaction report mail to {}", request.email());
    }

    public void importPosStatements(List<BankTransactionEntity> bankTransactions) {
        List<PosTransactionEntity> posTransactions = bankTransactions.stream().map(bt -> PosTransactionEntity.builder()
                .bankTransaction(BankTransactionEntity.builder().id(bt.getId()).build())
                .build()).toList();
        posTransactionRepository.saveAll(posTransactions);
    }

    private Path storeFile(MultipartFile file, UUID id) {
        try {
            Path dir = Path.of(posPath);
            Files.createDirectories(dir);
            Path target = dir.resolve(id + ".pdf");
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}