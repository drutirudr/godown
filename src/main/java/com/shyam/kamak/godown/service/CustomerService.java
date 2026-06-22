package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.CustomerRequestDTO;
import com.shyam.kamak.godown.dto.CustomerResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.CustomerMapper;
import com.shyam.kamak.godown.model.Customer;
import com.shyam.kamak.godown.repository.CustomerRepository;
import com.shyam.kamak.godown.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        // Enforce structural unique business rules pre-commit
        validateUniqueness(request.getName(), request.getContactNumber(), null);

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponseDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Ensure update parameters do not collide with another existing record
        validateUniqueness(request.getName(), request.getContactNumber(), id);

        customerMapper.updateEntityFromDto(request, existingCustomer);
        return customerMapper.toResponseDto(customerRepository.save(existingCustomer));
    }

    private void validateUniqueness(String name, String contactNumber, Long currentId) {
        customerRepository.findByNameAndContactNumber(name, contactNumber)
                .ifPresent(existingCustomer -> {
                    // During an update request, allow saving if the match belongs to the same record ID
                    if (currentId == null || !existingCustomer.getId().equals(currentId)) {
                        throw new IllegalStateException(
                                String.format("A customer entry with name '%s' and contact number '%s' already exists.", name, contactNumber)
                        );
                    }
                });
    }

    @Transactional(readOnly = true) public CustomerResponseDTO getCustomerById(Long id) { return customerRepository.findById(id).map(customerMapper::toResponseDto).orElseThrow(() -> new ResourceNotFoundException("Customer not found")); }
    @Transactional(readOnly = true) public List<CustomerResponseDTO> getAllCustomers() { return customerRepository.findAll().stream().map(customerMapper::toResponseDto).toList(); }
    @Transactional public void deleteCustomer(Long id) { Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found")); customerRepository.delete(customer); }
}
//@Service
//@RequiredArgsConstructor
//public class CustomerService {
//
//    private final CustomerRepository customerRepository;
//    private final CustomerMapper customerMapper; // Injected component
//
//    @Transactional
//    public CustomerDTO createCustomer(CustomerDTO dto) {
//        if (customerRepository.existsByCustomerNameAndContactNumber(dto.customerName(), dto.contactNumber())) {
//            throw new IllegalArgumentException("Customer with this name and contact number already exists.");
//        }
//        var customer = customerMapper.toEntity(dto);
//        return customerMapper.toDTO(customerRepository.save(customer));
//    }
//
//    @Transactional
//    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
//        var customer = customerRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
//
//        if (customerRepository.existsByCustomerNameAndContactNumberAndIdNot(dto.customerName(), dto.contactNumber(), id)) {
//            throw new IllegalArgumentException("Another customer with this name and contact number already exists.");
//        }
//
//        customerMapper.updateEntityFromDto(dto, customer); // In-place field updates
//        return customerMapper.toDTO(customerRepository.save(customer));
//    }
//
//    @Transactional(readOnly = true)
//    public CustomerDTO getCustomerById(Long id) {
//        return customerRepository.findById(id)
//                .map(customerMapper::toDTO)
//                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
//    }
//
//    @Transactional(readOnly = true)
//    public List<CustomerDTO> getAllCustomers() {
//        return customerRepository.findAll().stream()
//                .map(customerMapper::toDTO)
//                .toList();
//    }
//
//    @Transactional
//    public void deleteCustomerById(Long id) {
//        if (!customerRepository.existsById(id)) {
//            throw new IllegalArgumentException("Customer not found with id: " + id);
//        }
//        customerRepository.deleteById(id);
//    }
//}