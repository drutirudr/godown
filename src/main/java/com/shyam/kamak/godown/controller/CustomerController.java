package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.CustomerRequestDTO;
import com.shyam.kamak.godown.dto.CustomerResponseDTO;
import com.shyam.kamak.godown.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO request) {
        return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
//@RestController
//@RequestMapping("/api/v1/customers")
//@RequiredArgsConstructor
//public class CustomerController {
//
//    private final CustomerService customerService;
//
////    @PostMapping
////    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
////        var createdCustomer = customerService.createCustomer(customerDTO);
////        return new ResponseEntity<>((HttpHeaders) createdCustomer, HttpStatus.CREATED);
////    }
//
//    @PostMapping
//    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
//        var createdCustomer = customerService.createCustomer(customerDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<CustomerDTO> updateCustomerById(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
//        var updatedCustomer = customerService.updateCustomer(id, customerDTO);
//        return ResponseEntity.ok(updatedCustomer);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
//        var customerDTO = customerService.getCustomerById(id);
//        return ResponseEntity.ok(customerDTO);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
//        var customers = customerService.getAllCustomers();
//        return ResponseEntity.ok(customers);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id) {
//        customerService.deleteCustomerById(id);
//        return ResponseEntity.noContent().build(); // Modern standard returns 204 No Content for successful deletes
//    }
//}
