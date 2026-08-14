package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.BankStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankStatementRepository extends JpaRepository<BankStatementEntity, UUID> {

    List<BankStatementEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByStatementId(String statementId);
}
