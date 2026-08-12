package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.model.MonthlySummaryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummaryEntity, UUID> {

    Optional<MonthlySummaryEntity> findFirstByOrderByMonthDesc();

    List<MonthlySummaryEntity> findAllByOrderByMonthDesc(Pageable pageable);

    void deleteByMonthGreaterThanEqualAndMonthLessThan(LocalDate from, LocalDate to);
}