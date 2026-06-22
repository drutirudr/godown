package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "bill_fabric_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillFabricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fabric_name", nullable = false)
    private String fabricName;

    @Column(name = "fabric_width", nullable = false)
    private Double fabricWidth;

    @Column(name = "historical_cost", nullable = false)
    private Double historicalCost;
}
