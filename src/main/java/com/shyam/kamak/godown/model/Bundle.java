package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bundles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bundle_number", "financial_year"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bundle extends UserAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bundle_number", nullable = false, length = 50)
    private String bundleNumber;

    @Column(name = "financial_year", nullable = false, length = 10)
    private String financialYear;

    @Column(name = "manufacturer_code", nullable = false, length = 50)
    private String manufacturerCode;

    @Column(name = "is_sold", nullable = false)
    private boolean sold = false;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BundleItem> items = new ArrayList<>();

    @Version
    private Long version;

    public void addItem(BundleItem item) {
        items.add(item);
        item.setBundle(this);
    }

    public void removeItem(BundleItem item) {
        items.remove(item);
        item.setBundle(null);
    }
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
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "bundles", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"financial_year", "sequence_number"}),
//        @UniqueConstraint(columnNames = {"business_bundle_id"})
//})
//@EntityListeners(AuditingEntityListener.class)
//@Getter
//@Setter
//@NoArgsConstructor  // CRITICAL FIX FOR HIBERNATE REFLECTION
//@AllArgsConstructor // CRITICAL FIX FOR ENTITY BUILDERS
//@Builder
//public class Bundle {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "financial_year", nullable = false)
//    private String financialYear;
//
//    @Column(name = "sequence_number", nullable = false)
//    private Integer sequenceNumber;
//
//    @Column(name = "business_bundle_id", nullable = false)
//    private String businessBundleId;
//
//    @Column(name = "manufacturer_code", nullable = false)
//    private String manufacturerCode;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private BundleStatus status = BundleStatus.AVAILABLE;
//
//    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    private List<BundleItem> bundleItems = new ArrayList<>();
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
//    public void addBundleItem(BundleItem item) {
//        bundleItems.add(item);
//        item.setBundle(this);
//    }
//}
//
////@Entity @Table(name = "bundles")
////@Getter @Setter @NoArgsConstructor @AllArgsConstructor
////public class Bundle {
////    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
////    @Column(name = "financial_year", nullable = false) private String financialYear;
////    @Column(name = "sequence_number", nullable = false) private Integer sequenceNumber;
////    @Column(name = "business_bundle_id", nullable = false) private String businessBundleId;
////    @Column(name = "manufacturer_code", nullable = false) private String manufacturerCode;
////    @Enumerated(EnumType.STRING) @Column(nullable = false) private BundleStatus status = BundleStatus.AVAILABLE;
////
////    @JsonManagedReference
////    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, targetEntity = BundleItem.class)
////    private List<BundleItem> bundleItems = new ArrayList<>();
////
////    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
////    @Column(name = "updated_at") private LocalDateTime updatedAt = LocalDateTime.now();
////
////    public void addBundleItem(BundleItem item) { bundleItems.add(item); item.setBundle(this); }
////}