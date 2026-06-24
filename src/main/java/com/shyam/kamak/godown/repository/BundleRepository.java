package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long>, JpaSpecificationExecutor<Bundle> {

    Optional<Bundle> findByBundleNumberAndFinancialYear(String bundleNumber, String financialYear);

    @Query("SELECT COALESCE(MAX(CAST(b.bundleNumber AS int)), 0) FROM Bundle b WHERE b.financialYear = :fy")
    int findMaxBundleNumberByFinancialYear(@Param("fy") String financialYear);

    List<Bundle> findBySoldFalse();

    Page<Bundle> findBySoldFalseAndBundleNumberContainingIgnoreCase(String bundleNumber, Pageable pageable);
}
//@Repository
//public interface BundleRepository extends JpaRepository<Bundle, Long> {
//    @Query("SELECT COALESCE(MAX(b.sequenceNumber), 0) FROM Bundle b WHERE b.financialYear = :fy")
//    Integer findMaxSequenceByFinancialYear(@Param("fy") String financialYear);
//
//    java.util.Optional<Bundle> findByBusinessBundleId(String businessBundleId);
//}

//@Repository public interface CustomerRepository extends JpaRepository<Customer, Long> {}
//@Repository public interface FabricRepository extends JpaRepository<Fabric, Long> {}
//@Repository public interface SalesBillRepository extends JpaRepository<SalesBill, Long> {}
//@Repository public interface BundleRepository extends JpaRepository<Bundle, Long> {
//    java.util.Optional<Bundle> findByBusinessBundleId(String businessBundleId);
//}