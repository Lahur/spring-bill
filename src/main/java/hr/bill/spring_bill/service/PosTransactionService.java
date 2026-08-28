package hr.bill.spring_bill.service;

import hr.bill.spring_bill.clients.mail_bill.MailBillClient;
import hr.bill.spring_bill.dao.PosTransactionRepository;
import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.PosTransactionMapper;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.PosTransactionEntity;
import hr.bill.spring_bill.service.document.PosStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosTransactionService {

    private final PosTransactionRepository posTransactionRepository;

    private final PosTransactionMapper posTransactionMapper;

    private final MailBillClient mailBillClient;

    private final RecipientService recipientService;

    private final PosStrategy posStrategy;

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