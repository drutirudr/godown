package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Customer;
import com.shyam.kamak.godown.model.TypeOfBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeOfBillRepository extends JpaRepository<TypeOfBill, Long>, JpaSpecificationExecutor<TypeOfBill> {
    Optional<TypeOfBill> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
