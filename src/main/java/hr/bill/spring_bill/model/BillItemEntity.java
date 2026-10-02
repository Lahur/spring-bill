package hr.bill.spring_bill.model;

import hr.bill.spring_bill.dto.eposlovanje.enums.UnitOfMeasure;
import hr.bill.spring_bill.dto.eposlovanje.enums.VatCategory;
import jakarta.persistence.*;
import lombok.*;
import hr.bill.spring_bill.model.superclass.TenantScopedEntity;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_item")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemEntity extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_info_id", nullable = false)
    private BillInfoEntity billInfo;

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