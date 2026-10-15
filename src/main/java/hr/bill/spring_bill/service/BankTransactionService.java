package hr.bill.spring_bill.service;

import hr.bill.spring_bill.dao.BankTransactionRepository;
import hr.bill.spring_bill.dto.web.banktransaction.BankTransactionResponse;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.mapper.BankTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankTransactionService {

    private final BankTransactionRepository bankTransactionRepository;

    private final BankTransactionMapper bankTransactionMapper;

    public List<BankTransactionResponse> findAll() {
        log.debug("Fetching all bank transactions");
        List<BankTransactionResponse> result = bankTransactionMapper.toBankTransactionResponseList(
                bankTransactionRepository.findAllByOrderByTransactionDateDesc());
        log.debug("Found {} bank transactions", result.size());
        return result;
    }

    public List<BankTransactionResponse> findAllByBankStatementId(UUID bankStatementId) {
        log.debug("Fetching bank transactions for bank statement {}", bankStatementId);
        List<BankTransactionResponse> result = bankTransactionMapper.toBankTransactionResponseList(
                bankTransactionRepository.findAllByBankStatement_IdOrderByTransactionDateAsc(bankStatementId));
        log.debug("Found {} bank transactions for bank statement {}", result.size(), bankStatementId);
        return result;
    }

    public BankTransactionResponse findById(UUID id) {
        log.debug("Fetching bank transaction {}", id);
        return bankTransactionRepository.findById(id)
                .map(bankTransactionMapper::toBankTransactionResponse)
                .orElseThrow(() -> new NotFoundException("Bank transaction not found for id: " + id));
    }
}
