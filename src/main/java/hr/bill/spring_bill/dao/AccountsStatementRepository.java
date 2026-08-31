package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.AccountsStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountsStatementRepository extends JpaRepository<AccountsStatementEntity, UUID> {

    List<AccountsStatementEntity> findAllByOrderByDateDesc();

    List<AccountsStatementEntity> findAllByIdInOrderByDateDesc(List<UUID> ids);
}