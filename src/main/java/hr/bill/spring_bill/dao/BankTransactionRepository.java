package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.BankTransactionEntity;
import hr.bill.spring_bill.model.enums.CreditDebitIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankTransactionRepository extends JpaRepository<BankTransactionEntity, UUID> {

    List<BankTransactionEntity> findAllByCreditDebitIndicatorAndReceiverIbanIgnoreCase(
            CreditDebitIndicator creditDebitIndicator, String receiverIban);
}
