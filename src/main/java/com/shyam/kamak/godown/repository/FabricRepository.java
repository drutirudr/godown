package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FabricRepository extends JpaRepository<Fabric, Long>, JpaSpecificationExecutor<Fabric> {
    // Core structural check to protect our multi-column constraint logic
    Optional<Fabric> findByNameAndWidth(String name, BigDecimal width);
}
