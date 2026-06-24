package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    // Proactive unique check query
    Optional<Customer> findByNameAndContactNumber(String name, String contactNumber);
}

//public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
//    // This interface now automatically supports building dynamic multi-column queries
//}
//import com.shyam.kamak.godown.model.*;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface CustomerRepository extends JpaRepository<Customer, Long> {
//    boolean existsByCustomerNameAndContactNumber(String customerName, String contactNumber);
//    boolean existsByCustomerNameAndContactNumberAndIdNot(String customerName, String contactNumber, Long id);
//}