package hr.bill.spring_bill.model;

import hr.bill.spring_bill.dto.eposlovanje.enums.DocumentType;
import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bill_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bill_id", nullable = false, unique = true)
    private UUID billId;

    // mainDataInfo
    @Column(name = "main_bill_date")
    private LocalDate mainBillDate;

    @Column(name = "main_due_date")
    private LocalDate mainDueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50)
    private DocumentType documentType;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "bill_period_from")
    private LocalDate billPeriodFrom;

    @Column(name = "bill_period_till")
    private LocalDate billPeriodTill;

    // buyerInfo
    @Column(name = "buyer_name")
    private String buyerName;

    @Column(name = "buyer_oib", length = 11)
    private String buyerOib;

    @Column(name = "buyer_address")
    private String buyerAddress;

    @Column(name = "buyer_city")
    private String buyerCity;

    @Column(name = "buyer_postal_code", length = 20)
    private String buyerPostalCode;

    // supplierInfo
    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_oib", length = 11)
    private String supplierOib;

    @Column(name = "supplier_address")
    private String supplierAddress;

    @Column(name = "supplier_city")
    private String supplierCity;

    @Column(name = "supplier_postal_code", length = 20)
    private String supplierPostalCode;

    @Column(name = "supplier_contact_name")
    private String supplierContactName;

    @Column(name = "supplier_contact_oib", length = 11)
    private String supplierContactOib;

    @Column(name = "supplier_contact_email")
    private String supplierContactEmail;

    @Column(name = "supplier_contact_phone", length = 50)
    private String supplierContactPhone;

    // paymentInfo
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_means", length = 50)
    private PaymentMeans paymentMeans;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Column(name = "payment_iban", length = 34)
    private String paymentIban;

    @Column(name = "payment_model", length = 10)
    private String paymentModel;

    @Column(name = "payment_reference", length = 50)
    private String paymentReference;

    @Column(name = "payment_note")
    private String paymentNote;

    // priceInfo
    @Column(name = "vat_exclusive_amount", precision = 19, scale = 2)
    private BigDecimal vatExclusiveAmount;

    @Column(name = "vat_amount", precision = 19, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "vat_inclusive_amount", precision = 19, scale = 2)
    private BigDecimal vatInclusiveAmount;

    @Column(name = "advance_amount", precision = 19, scale = 2)
    private BigDecimal advanceAmount;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;
}