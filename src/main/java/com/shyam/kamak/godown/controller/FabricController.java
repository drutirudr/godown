package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.FabricRequestDTO;
import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.service.FabricService;
import com.shyam.kamak.godown.service.SalesBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public ResponseEntity<List<FabricResponseDTO>> getAllFabrics() {
        return ResponseEntity.ok(fabricService.getAllFabrics());
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
