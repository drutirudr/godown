package com.shyam.kamak.godown.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_name_contact", columnNames = {"name", "contact_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends UserAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Version
    private Long version;
}
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedBy;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.Instant;
//
//@Entity
//@Table(name = "customers", uniqueConstraints = {
//        @UniqueConstraint(name = "uq_name_contact", columnNames = {"customer_name", "contact_number"})
//})
//@EntityListeners(AuditingEntityListener.class) // Crucial for lifecycle callbacks
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Customer {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "customer_name", nullable = false, length = 100)
//    private String customerName;
//
//    @Column(name = "contact_number", nullable = false, length = 15)
//    private String contactNumber;
//
//    @Column(nullable = false, length = 255)
//    private String address;
//
//    @Column(nullable = false, length = 50)
//    private String city;
//
//    @Column(nullable = false, length = 50)
//    private String state;
//
//    @Column(nullable = false, length = 10)
//    private String pincode;
//
//    // Audit fields
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
