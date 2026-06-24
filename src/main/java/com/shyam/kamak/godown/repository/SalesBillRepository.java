package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.model.SalesBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SalesBillRepository extends JpaRepository<SalesBill, Long>, JpaSpecificationExecutor<SalesBill> {

    @Query("SELECT COALESCE(MAX(CAST(sb.billNumber AS int)), 0) FROM SalesBill sb WHERE sb.financialYear = :fy")
    int findMaxBillNumberByFinancialYear(@Param("fy") String financialYear);

    Optional<SalesBill> findByBillNumberAndFinancialYear(String billNumber, String financialYear);
}
//import com.shyam.kamak.godown.model.*;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//
//@Repository
//public interface SalesBillRepository extends JpaRepository<SalesBill, Long> {
//    @Query("SELECT COALESCE(MAX(s.billSequenceNumber), 0) FROM SalesBill s WHERE s.financialYear = :fy")
//    Integer findMaxSequenceByFinancialYear(@Param("fy") String financialYear);
//}

