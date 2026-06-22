package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_bills", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sales_bill_fy", columnNames = {"bill_number", "financial_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesBill extends UserAuditable {

    public enum CalculationType { FLAT, PERCENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, length = 50)
    private String billNumber;

    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private CalculationType discountType;

    @Column(name = "discount_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 20)
    private CalculationType taxType;

    @Column(name = "tax_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "salesBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SalesBillItem> items = new ArrayList<>();

    @Version
    private Long version;

    public void addItem(SalesBillItem item) {
        items.add(item);
        item.setSalesBill(this);
    }

    public void removeItem(SalesBillItem item) {
        items.remove(item);
        item.setSalesBill(null);
    }
}
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedBy;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "sales_bills", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"financial_year", "bill_sequence_number"}),
//        @UniqueConstraint(columnNames = {"business_bill_number"})
//})
//@EntityListeners(AuditingEntityListener.class)
//@Getter
//@Setter
//@NoArgsConstructor  // CRITICAL FIX FOR HIBERNATE REFLECTION
//@AllArgsConstructor // CRITICAL FIX FOR ENTITY BUILDERS
//@Builder
//public class SalesBill {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "financial_year", nullable = false)
//    private String financialYear;
//
//    @Column(name = "bill_sequence_number", nullable = false)
//    private Integer billSequenceNumber;
//
//    @Column(name = "business_bill_number", nullable = false)
//    private String businessBillNumber;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private Customer customer;
//
//    @Column(name = "bill_date", nullable = false)
//    private Instant billDate = Instant.now();
//
//    @Column(name = "sub_total", nullable = false, precision = 14, scale = 2)
//    private BigDecimal subTotal = BigDecimal.ZERO;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "discount_type", nullable = false)
//    private DiscountType discountType = DiscountType.NONE;
//
//    @Column(name = "discount_rate", nullable = false, precision = 14, scale = 2)
//    private BigDecimal discountRate = BigDecimal.ZERO;
//
//    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
//    private BigDecimal discountAmount = BigDecimal.ZERO;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "tax_type", nullable = false)
//    private TaxType taxType = TaxType.NONE;
//
//    @Column(name = "tax_rate_percent", nullable = false, precision = 5, scale = 2)
//    private BigDecimal taxRatePercent = BigDecimal.ZERO;
//
//    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
//    private BigDecimal taxAmount = BigDecimal.ZERO;
//
//    @Column(name = "grand_total", nullable = false, precision = 14, scale = 2)
//    private BigDecimal grandTotal = BigDecimal.ZERO;
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "salesBill", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = SalesBillItem.class)
//    private List<SalesBillItem> salesBillItems = new ArrayList<>();
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at", nullable = false)
//    private Instant updatedAt;
//
//    @CreatedBy
//    @Column(name = "created_by", updatable = false, length = 50)
//    private String createdBy;
//
//    @LastModifiedBy
//    @Column(name = "updated_by", length = 50)
//    private String updatedBy;
//
//    public void setSalesBillItems(List<SalesBillItem> items) {
//        this.salesBillItems.clear();
//        if (items != null) {
//            items.forEach(item -> {
//                item.setSalesBill(this);
//                this.salesBillItems.add(item);
//            });
//        }
//    }
//}
//
////@Entity @Table(name = "sales_bills")
////@Getter @Setter @NoArgsConstructor @AllArgsConstructor
////public class SalesBill {
////    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
////    @Column(name = "financial_year", nullable = false) private String financialYear;
////    @Column(name = "bill_sequence_number", nullable = false) private Integer billSequenceNumber;
////    @Column(name = "business_bill_number", nullable = false) private String businessBillNumber;
////    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "customer_id", nullable = false) private Customer customer;
////    @Column(name = "bill_date", nullable = false) private LocalDateTime billDate = LocalDateTime.now();
////    @Column(name = "sub_total", nullable = false, precision = 14, scale = 2) private BigDecimal subTotal = BigDecimal.ZERO;
////    @Enumerated(EnumType.STRING) @Column(name = "discount_type", nullable = false) private DiscountType discountType = DiscountType.NONE;
////    @Column(name = "discount_rate", nullable = false, precision = 14, scale = 2) private BigDecimal discountRate = BigDecimal.ZERO;
////    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2) private BigDecimal discountAmount = BigDecimal.ZERO;
////    @Enumerated(EnumType.STRING) @Column(name = "tax_type", nullable = false) private TaxType taxType = TaxType.NONE;
////    @Column(name = "tax_rate_percent", nullable = false, precision = 5, scale = 2) private BigDecimal taxRatePercent = BigDecimal.ZERO;
////    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2) private BigDecimal taxAmount = BigDecimal.ZERO;
////    @Column(name = "grand_total", nullable = false, precision = 14, scale = 2) private BigDecimal grandTotal = BigDecimal.ZERO;
////
////    @JsonManagedReference
////    @OneToMany(mappedBy = "salesBill", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = SalesBillItem.class)
////    private List<SalesBillItem> salesBillItems = new ArrayList<>();
////
////    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
////    @Column(name = "updated_at") private LocalDateTime updatedAt = LocalDateTime.now();
////
////    public void setSalesBillItems(List<SalesBillItem> items) {
////        this.salesBillItems.clear();
////        if (items != null) { items.forEach(item -> { item.setSalesBill(this); this.salesBillItems.add(item); }); }
////    }
////}
//
