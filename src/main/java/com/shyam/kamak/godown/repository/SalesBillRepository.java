package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.SalesBill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
// 🚀 FIXED: Extends JpaRepository and your Custom Interface layout cleanly with zero clashes!
public interface SalesBillRepository extends JpaRepository<SalesBill, Long>, SalesBillRepositoryCustom {

    @Query("SELECT s FROM SalesBill s WHERE s.billNumber = :billNumber AND s.billDate BETWEEN :startDate AND :endDate")
    Optional<SalesBill> findDuplicateInFinancialYear(
            @Param("billNumber") String billNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @EntityGraph(attributePaths = {"items", "items.bundle", "customer", "typeOfBill"})
    @Query("SELECT s FROM SalesBill s WHERE s.id = :id")
    Optional<SalesBill> findWithDetailsById(@Param("id") Long id);
}
