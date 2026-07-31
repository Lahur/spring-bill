package hr.bill.spring_bill.model;

import hr.bill.spring_bill.dto.eposlovanje.enums.UnitOfMeasure;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bill_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bill_info_id", nullable = false)
    private UUID billInfoId;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", length = 10)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "base_amount", precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_category", length = 50)
    private VatCategory vatCategory;
}