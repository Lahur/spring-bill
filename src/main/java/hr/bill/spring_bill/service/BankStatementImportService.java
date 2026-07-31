package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.BankStatementRepository;
import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.mapper.BankStatementMapper;
import hr.bill.spring_bill.mapper.CamtStatementMapper;
import hr.bill.spring_bill.model.BankStatementEntity;
import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.service.document.BillStrategyFactory;
import hr.bill.spring_bill.xml.camt.model.CamtDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class BankStatementImportService {

    private final CamtXmlService camtXmlService;

    private final CamtStatementMapper camtStatementMapper;

    private final BankStatementRepository bankStatementRepository;

    private final BankTransactionRepository bankTransactionRepository;

    private final BillStrategyFactory billStrategyFactory;

    private final BankStatementMapper bankStatementMapper;

    public List<BankStatementResponse> findAll() {
        return bankStatementMapper.toBankStatementResponseList(bankStatementRepository.findAllByOrderByCreatedAtDesc());
    }

    public BankStatementResponse importStatement(MultipartFile file) {
        return importStatementXml(readXml(file));
    }

    public List<BankStatementResponse> importStatements(List<MultipartFile> files) {
        List<BankStatementResponse> imported = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".zip")) {
                imported.addAll(importStatementsZip(file));
            } else {
                imported.add(importStatement(file));
            }
        }
        return imported;
    }

    public List<BankStatementResponse> importStatementsZip(MultipartFile zipFile) {
        List<BankStatementResponse> imported = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    imported.add(importStatementXml(xml));
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return imported;
    }

    private BankStatementResponse importStatementXml(String xml) {
        CamtDocument document = camtXmlService.parse(xml);

        BankStatementEntity statement = bankStatementRepository.save(camtStatementMapper.toBankStatementEntity(document));

        List<BankTransactionEntity> transactions = camtStatementMapper.toBankTransactionEntities(document, statement.getId());
        bankTransactionRepository.saveAll(transactions);

        billStrategyFactory.markPaidFromBankStatement(document);

        return bankStatementMapper.toBankStatementResponse(statement);
    }

    private String readXml(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
