package hr.bill.spring_bill.service.document;

import hr.bill.spring_bill.clients.bill_pdf.BillPdfClient;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.web.BillReportType;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.PosTransactionMapper;
import hr.bill.spring_bill.model.PosTransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PosStrategy implements DocumentStrategy {

    private final PosTransactionRepository posTransactionRepository;

    private final PosTransactionMapper posTransactionMapper;

    private final BillPdfClient billPdfClient;

    @Override
    public BillReportType getType() {
        return BillReportType.POS;
    }

    @Override
    public BillDocument createDocument(String id) {
        log.debug("Creating document for POS transaction {}", id);
        PosTransactionEntity entity = posTransactionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("POS transaction not found for id: " + id));

        byte[] reportContent = billPdfClient.renderPosTransaction(posTransactionMapper.toPosTransactionRequest(entity));
        byte[] content = entity.getBillPath() == null
                ? reportContent
                : mergeWithBill(reportContent, Path.of(entity.getBillPath()));

        return BillDocument.builder()
                .content(content)
                .filename("pos-" + entity.getId())
                .build();
    }

    @Override
    public void incrementSentCount(String id) {
        log.debug("Incrementing sent count for POS transaction {}", id);
        PosTransactionEntity entity = posTransactionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("POS transaction not found for id: " + id));
        entity.setSentCount(entity.getSentCount() + 1);
        posTransactionRepository.save(entity);
    }

    private byte[] mergeWithBill(byte[] reportContent, Path billPath) {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            merger.setDestinationStream(mergedOutput);
            merger.addSource(new RandomAccessReadBuffer(reportContent));
            merger.addSource(billPath.toFile());
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
            return mergedOutput.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
