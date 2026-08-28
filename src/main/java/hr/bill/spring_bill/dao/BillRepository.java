package hr.bill.spring_bill.dao;

import hr.bill.spring_bill.dao.projection.DailyTotalProjection;
import hr.bill.spring_bill.model.BillEntity;
import hr.bill.spring_bill.model.enums.BillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<BillEntity, UUID> {

    List<BillEntity> findAllByBillTypeOrderByBillDateDesc(BillType billType);

    Optional<BillEntity> findFirstByBillTypeOrderByBillDateDesc(BillType billType);

    Optional<BillEntity> findFirstByBillTypeOrderByBillDateAsc(BillType billType);

    Optional<BillEntity> findBySystemIdAndBillType(Long systemId, BillType billType);

    @Transactional
    void deleteAllByBillType(BillType billType);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM bill " +
            "WHERE bill_type <> :billType AND bill_date >= :from AND bill_date < :to " +
            "AND document_status = 'PlacenUPotpunosti'", nativeQuery = true)
    BigDecimal sumPaidAmountByBillTypeNotAndBillDateBetween(@Param("billType") String billType,
                                                             @Param("from") LocalDateTime from,
                                                             @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM bill " +
            "WHERE bill_type <> :billType AND bill_date >= :from AND bill_date < :to " +
            "AND (document_status IS NULL OR document_status <> 'PlacenUPotpunosti')", nativeQuery = true)
    BigDecimal sumUnpaidAmountByBillTypeNotAndBillDateBetween(@Param("billType") String billType,
                                                               @Param("from") LocalDateTime from,
                                                               @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM bill " +
            "WHERE bill_type = :billType AND bill_date >= :from AND bill_date < :to " +
            "AND document_status = 'PlacenUPotpunosti'", nativeQuery = true)
    BigDecimal sumPaidAmountByBillTypeAndBillDateBetween(@Param("billType") String billType,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM bill " +
            "WHERE bill_type = :billType AND bill_date >= :from AND bill_date < :to " +
            "AND (document_status IS NULL OR document_status <> 'PlacenUPotpunosti')", nativeQuery = true)
    BigDecimal sumUnpaidAmountByBillTypeAndBillDateBetween(@Param("billType") String billType,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);

    @Query(value = "SELECT * FROM bill " +
            "WHERE bill_type = :billType AND bill_date >= :from AND bill_date < :to", nativeQuery = true)
    List<BillEntity> findAllByBillTypeAndBillDateBetween(@Param("billType") String billType,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    @Query("SELECT b FROM BillEntity b " +
            "WHERE b.billType = :billType " +
            "AND (:from IS NULL OR b.billDate >= :from) " +
            "AND (:to IS NULL OR b.billDate < :to) " +
            "ORDER BY b.billDate DESC")
    List<BillEntity> findAllByBillTypeAndBillDateBetweenOptional(@Param("billType") BillType billType,
                                                                  @Param("from") LocalDateTime from,
                                                                  @Param("to") LocalDateTime to);

    @Query(value = "SELECT CAST(bill_date AS date) AS day, COALESCE(SUM(total_amount), 0) AS total FROM bill " +
            "WHERE bill_type <> :billType AND bill_date >= :from AND bill_date < :to " +
            "GROUP BY CAST(bill_date AS date)", nativeQuery = true)
    List<DailyTotalProjection> sumDailyTotalsByBillTypeNot(@Param("billType") String billType,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);
}