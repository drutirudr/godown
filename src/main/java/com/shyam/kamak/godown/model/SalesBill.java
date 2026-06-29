package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_bills")
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

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_of_bill_id", nullable = false)
    private TypeOfBill typeOfBill;

    @Column(name = "lr_number", length = 50)
    private String lrNumber;

    @Column(name = "lr_date", length = 20)
    private String lrDate;

    @Column(name = "transporter_name", length = 150)
    private String transporterName;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "eway_bill_number", length = 50)
    private String ewayBillNumber;

    @Column(name = "e_invoice_number", length = 100)
    private String eInvoiceNumber;

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

    /**
     * Runtime calculated Financial Year based on billDate.
     * Essential hook for driving sequential tracking bounds securely in code.
     */
    @Transient
    public String getFinancialYear() {
        if (this.billDate == null) return null;

        int year = this.billDate.getYear();
        int month = this.billDate.getMonthValue();

        if (month < 4) {
            return "FY" + ((year - 1) % 100) + "-" + (year % 100);
        } else {
            return "FY" + (year % 100) + "-" + ((year + 1) % 100);
        }
    }

    public void addItem(SalesBillItem item) {
        items.add(item);
        item.setSalesBill(this);
    }

    public void removeItem(SalesBillItem item) {
        items.remove(item);
        item.setSalesBill(null);
    }
}