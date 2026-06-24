package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.CustomerResponseDTO;
import com.shyam.kamak.godown.dto.FabricRequestDTO;
import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.model.Customer;
import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.service.FabricService;
import com.shyam.kamak.godown.service.SalesBillService;
import com.shyam.kamak.godown.specification.CustomerSpecification;
import com.shyam.kamak.godown.specification.FabricSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fabrics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class FabricController {

    private final FabricService fabricService;

    @PostMapping
    public ResponseEntity<FabricResponseDTO> createFabric(@Valid @RequestBody FabricRequestDTO request) {
        return new ResponseEntity<>(fabricService.createFabric(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FabricResponseDTO> updateFabric(
            @PathVariable Long id,
            @Valid @RequestBody FabricRequestDTO request) {
        return ResponseEntity.ok(fabricService.updateFabric(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FabricResponseDTO> getFabricById(@PathVariable Long id) {
        return ResponseEntity.ok(fabricService.getFabricById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<FabricResponseDTO>> getAllFabrics() {
        return ResponseEntity.ok(fabricService.getAllFabrics());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<FabricResponseDTO>> getAllFabrics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,                      // Catches global text searches
            @RequestParam(required = false) String id,                          // Catches ID columns
            @RequestParam(required = false) String name,                        // Catches Customer Name fields
            @RequestParam(required = false) String width,                       // Catches width strings
            @RequestParam(required = false) String currentCostPerMeter) {       // Catches current cost per meter

        // 1. Optimize row scanning efficiency by enforcing pagination sorting boundaries
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // 2. Compile dynamic constraints mapping blocks
        Specification<Fabric> spec = FabricSpecification.getDynamicSearchCriteria(
                search, id, name, width, currentCostPerMeter
        );

        // 3. Extract and return the small, 20-row page slice instantly
        Page<FabricResponseDTO> responseData = fabricService.getAllFabricsPaged(spec, pageable);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFabric(@PathVariable Long id) {
        fabricService.deleteFabric(id);
        return ResponseEntity.noContent().build();
    }
}
//import com.shyam.kamak.godown.service.FabricService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/v1/fabrics")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class FabricController {
//
//    private final FabricService fabricService;
//
//    @PostMapping
//    public ResponseEntity<FabricDTO> createFabric(@Valid @RequestBody FabricDTO dto) {
//        log.info("Received request to create fabric: {}", dto);
//        return new ResponseEntity<>(fabricService.createFabric(dto), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<FabricDTO> updateFabric(@PathVariable Long id, @RequestBody FabricDTO dto) {
//        return ResponseEntity.ok(fabricService.updateFabric(id, dto));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<FabricDTO> getFabric(@PathVariable Long id) {
//        return ResponseEntity.ok(fabricService.getFabricById(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<FabricDTO>> getAllFabrics() {
//        return ResponseEntity.ok(fabricService.getAllFabrics());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteFabric(@PathVariable Long id) {
//        fabricService.deleteFabric(id);
//        return ResponseEntity.ok("Fabric profile was successfully removed from the master inventory system.");
//    }
//
////    @PostMapping public ResponseEntity<FabricDTO> create(@RequestBody FabricDTO dto) { return ResponseEntity.ok(fabricService.createFabric(dto)); }
////    @GetMapping public ResponseEntity<List<FabricDTO>> getAll() { return ResponseEntity.ok(fabricService.getAllFabrics()); }
//
//
//}
