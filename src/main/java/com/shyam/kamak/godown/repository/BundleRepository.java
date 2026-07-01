package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long>, JpaSpecificationExecutor<Bundle>, CustomBundleRepository  {

    List<Bundle> findBySoldFalse();

    Page<Bundle> findBySoldFalseAndBundleNumberContainingIgnoreCase(String bundleNumber, Pageable pageable);

//    @Query("SELECT COALESCE(MAX(CAST(b.bundleNumber AS int)), 0) FROM Bundle b WHERE b.bundleDate BETWEEN :startDate AND :endDate")
//    int findMaxBundleNumberByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    /**
     * 🚀 HIGH-PERFORMANCE STRING-EXTRACTING NATIVE QUERY
     * For a bundle number like 'BUN-20260518-4999996', this splits the text by hyphens,
     * grabs the 3rd token ('4999996'), casts it safely to an integer, and calculates the true MAX sequence.
     */
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(b.bundle_number, '-', -1) AS UNSIGNED)), 0) " +
            "FROM bundles b " +
            "WHERE b.bundle_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    int findMaxBundleNumberByDateRange(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );


    // Replaces the old findByBundleNumberAndFinancialYear call
    @Query("SELECT b FROM Bundle b WHERE b.bundleNumber = :bundleNumber AND b.bundleDate BETWEEN :startDate AND :endDate")
    Optional<Bundle> findByBundleNumberAndDateRange(
            @Param("bundleNumber") String bundleNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



    /**
     * Optimized Single-Trip Fetch:
     * Uses EntityGraph to batch-load the bundle and its sub-items
     * in one query, instantly fixing the row-click hang.
     */
    @EntityGraph(attributePaths = {"items", "items.fabric"})
    @Query("SELECT b FROM Bundle b WHERE b.id = :id")
    Optional<Bundle> findWithItemsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.fabric"})
    @Query("SELECT b FROM Bundle b WHERE b.bundleNumber IN :numbers AND b.isSold = false")
    List<Bundle> findAvailableBatchByNumbers(@Param("numbers") List<String> numbers);

//    /**
//     * 🚀 HIGH-PERFORMANCE SPECIFICATION SLICE FETCH FOR 5M+ ROWS
//     * 1. Returning 'Slice' cleanly blocks the heavy database COUNT(*) query calculation completely.
//     * 2. @EntityGraph forces a single Left Join fetch for items and fabrics, killing the N+1 loop storm.
//     */
//    @EntityGraph(attributePaths = {"items", "items.fabric"})
//    Slice<Bundle> findBy(Specification<Bundle> spec, Pageable pageable);
//
//    @EntityGraph(attributePaths = {"items", "items.fabric"})
//    @Query("SELECT b FROM Bundle b")
//    Slice<Bundle> fetchSliceWithGraph(Specification<Bundle> spec, Pageable pageable);
}
