package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.SalesBill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SalesBillRepository extends JpaRepository<SalesBill, Long>, JpaSpecificationExecutor<SalesBill> {

    @Query("SELECT s FROM SalesBill s WHERE s.billNumber = :billNumber AND s.billDate BETWEEN :startDate AND :endDate")
    Optional<SalesBill> findDuplicateInFinancialYear(
            @Param("billNumber") String billNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 🚀 HIGH-SPEED GRAPH SLICE TUNER: Fetches paginated sub-data arrays in exactly 1 single database pass!
    // Shifts completely from Page<T> to Slice<T> to completely wipe out the heavy COUNT(*) bottleneck.
    @EntityGraph(attributePaths = {"customer", "typeOfBill", "items", "items.bundle"})
    @Query("SELECT s FROM SalesBill s")
    Slice<SalesBill> fetchSliceWithGraph(Specification<SalesBill> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.bundle", "customer", "typeOfBill"})
    @Query("SELECT s FROM SalesBill s WHERE s.id = :id")
    Optional<SalesBill> findWithDetailsById(@Param("id") Long id);
}
