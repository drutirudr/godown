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
    private String color;

    @Column(name = "number_of_rolls", nullable = false)
    private Integer numberOfRolls;

    @Column(name = "meters_per_roll", nullable = false, precision = 8, scale = 2)
    private BigDecimal metersPerRoll;

    @Column(name = "frozen_cost_per_meter", nullable = false, precision = 10, scale = 2)
    private BigDecimal frozenCostPerMeter;
}
