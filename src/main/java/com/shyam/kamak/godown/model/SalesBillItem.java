package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_bill_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesBillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_bill_id", nullable = false)
    private SalesBill salesBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private Bundle bundle;

    @Column(name = "total_rolls", nullable = false)
    private Integer totalRolls;

    @Column(name = "total_meters", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalMeters;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "sales_bill_items", uniqueConstraints = @UniqueConstraint(columnNames = {"sales_bill_id", "bundle_id"}))
//@Getter
//@Setter
//public class SalesBillItem {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @JsonBackReference
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "sales_bill_id", nullable = false)
//    private SalesBill salesBill;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "bundle_id", nullable = false)
//    private Bundle bundle;
//
//    @Column(name = "snapshot_total_meters", nullable = false, precision = 12, scale = 2)
//    private BigDecimal snapshotTotalMeters;
//
//    @Column(name = "snapshot_bundle_subtotal", nullable = false, precision = 14, scale = 2)
//    private BigDecimal snapshotBundleSubtotal;
//}


//@Entity @Table(name = "sales_bill_items")
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor
//public class SalesBillItem {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
//    @JsonBackReference @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sales_bill_id", nullable = false) private SalesBill salesBill;
//    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "bundle_id", nullable = false) private Bundle bundle;
//    @Column(name = "snapshot_total_meters", nullable = false, precision = 12, scale = 2) private BigDecimal snapshotTotalMeters;
//    @Column(name = "snapshot_bundle_subtotal", nullable = false, precision = 14, scale = 2) private BigDecimal snapshotBundleSubtotal;
//}