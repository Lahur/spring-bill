package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.BillInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillInfoRepository extends JpaRepository<BillInfoEntity, UUID> {

    Optional<BillInfoEntity> findByBillId(UUID billId);
}