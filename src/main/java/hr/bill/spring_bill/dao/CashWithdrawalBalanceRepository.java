package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CashWithdrawalBalanceRepository extends JpaRepository<CashWithdrawalBalanceEntity, UUID> {

    List<CashWithdrawalBalanceEntity> findAllByBalanceGreaterThanOrderByBankTransaction_TransactionDateDesc(
            BigDecimal balance);
}