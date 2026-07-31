package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.BillItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillItemRepository extends JpaRepository<BillItemEntity, UUID> {

    List<BillItemEntity> findAllByBillInfoIdOrderByItemOrder(UUID billInfoId);
}