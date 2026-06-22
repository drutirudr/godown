package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Proactive unique check query
    Optional<Customer> findByNameAndContactNumber(String name, String contactNumber);
}

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