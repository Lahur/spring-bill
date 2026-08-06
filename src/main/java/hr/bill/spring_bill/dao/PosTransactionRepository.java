package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.PosTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PosTransactionRepository extends JpaRepository<PosTransactionEntity, UUID> {

    List<PosTransactionEntity> findAllByOrderByBankTransaction_TransactionDateDesc();
}