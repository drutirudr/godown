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
//
//@Entity
//@Table(name = "fabrics", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "width_inches"}))
//@EntityListeners(AuditingEntityListener.class)
//@Getter
//@Setter
//@NoArgsConstructor  // CRITICAL FIX FOR HIBERNATE REFLECTION
//@AllArgsConstructor // CRITICAL FIX FOR ENTITY BUILDERS
//@Builder
//public class Fabric {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(name = "width_inches", nullable = false, precision = 5, scale = 2)
//    private BigDecimal widthInches;
//
//    @Column(name = "current_price_per_meter", nullable = false, precision = 10, scale = 2)
//    private BigDecimal currentPricePerMeter;
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
//}
//
////@Entity @Table(name = "fabrics")
////@Getter @Setter @NoArgsConstructor @AllArgsConstructor
////public class Fabric {
////    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
////    @Column(nullable = false) private String name;
////    @Column(name = "width_inches", nullable = false, precision = 5, scale = 2) private BigDecimal widthInches;
////    @Column(name = "current_price_per_meter", nullable = false, precision = 10, scale = 2) private BigDecimal currentPricePerMeter;
////    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
////    @Column(name = "updated_at") private LocalDateTime updatedAt = LocalDateTime.now();
////}