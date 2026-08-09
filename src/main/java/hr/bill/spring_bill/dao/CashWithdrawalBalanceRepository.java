package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CashWithdrawalBalanceRepository extends JpaRepository<CashWithdrawalBalanceEntity, UUID> {
}