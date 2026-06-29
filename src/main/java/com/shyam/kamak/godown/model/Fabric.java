package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fabrics", uniqueConstraints = {
        @UniqueConstraint(name = "uq_fabric_name_width", columnNames = {"name", "width"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fabric extends UserAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal width;

    @Column(name = "current_cost_per_meter", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentCostPerMeter;

    @Version
    private Long version;
}