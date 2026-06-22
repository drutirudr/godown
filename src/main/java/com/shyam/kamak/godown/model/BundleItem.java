package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bundle_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private Bundle bundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabric_id", nullable = false)
    private Fabric fabric;

    @Column(nullable = false, length = 50)
    private String color; // Added here

    @Column(name = "number_of_rolls", nullable = false)
    private Integer numberOfRolls;

    @Column(name = "meters_per_roll", nullable = false, precision = 8, scale = 2)
    private BigDecimal metersPerRoll;

    @Column(name = "frozen_cost_per_meter", nullable = false, precision = 10, scale = 2)
    private BigDecimal frozenCostPerMeter;
}
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import org.hibernate.annotations.Generated;
//import org.hibernate.generator.EventType;
//
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "bundle_items")
//@Getter
//@Setter
//public class BundleItem {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @JsonBackReference
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "bundle_id", nullable = false)
//    private Bundle bundle;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "fabric_id", nullable = false)
//    private Fabric fabric;
//
//    @Column(name = "num_rolls", nullable = false)
//    private Integer numRolls;
//
//    @Column(name = "meters_per_roll", nullable = false, precision = 10, scale = 2)
//    private BigDecimal metersPerRoll;
//
//    @Column(name = "snapshot_price_per_meter", nullable = false, precision = 10, scale = 2)
//    private BigDecimal snapshotPricePerMeter;
//
//    @Generated(event = {EventType.INSERT, EventType.UPDATE})
//    @Column(name = "total_meters", insertable = false, updatable = false)
//    private BigDecimal totalMeters;
//
//    @Column(name = "item_subtotal", insertable = false, updatable = false)
//    private BigDecimal itemSubtotal;
//
//    @Column(nullable = false)
//    private String color;
//}
//
////@Entity @Table(name = "bundle_items")
////@Getter @Setter @NoArgsConstructor @AllArgsConstructor
////public class BundleItem {
////    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
////    @JsonBackReference @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bundle_id", nullable = false) private Bundle bundle;
////    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "fabric_id", nullable = false) private Fabric fabric;
////    @Column(name = "num_rolls", nullable = false) private Integer numRolls;
////    @Column(name = "meters_per_roll", nullable = false, precision = 10, scale = 2) private BigDecimal metersPerRoll;
////    @Column(name = "snapshot_price_per_meter", nullable = false, precision = 10, scale = 2) private BigDecimal snapshotPricePerMeter;
////
////    @Generated(event = {EventType.INSERT, EventType.UPDATE}) @Column(name = "total_meters", insertable = false, updatable = false) private BigDecimal totalMeters;
////    @Generated(event = {EventType.INSERT, EventType.UPDATE}) @Column(name = "item_subtotal", insertable = false, updatable = false) private BigDecimal itemSubtotal;
////    @Column(nullable = false) private String color;
////}

