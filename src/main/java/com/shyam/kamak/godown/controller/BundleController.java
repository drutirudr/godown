package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.BundleRequestDTO;
import com.shyam.kamak.godown.dto.BundleResponseDTO;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.service.BundleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bundles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Update with specific React domain in production
public class BundleController {

    private final BundleService bundleService;

    @PostMapping
    public ResponseEntity<BundleResponseDTO> createBundle(@Valid @RequestBody BundleRequestDTO request) {
        return new ResponseEntity<>(bundleService.createBundle(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BundleResponseDTO> updateBundle(
            @PathVariable Long id,
            @Valid @RequestBody BundleRequestDTO request) {
        return ResponseEntity.ok(bundleService.updateBundle(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundleResponseDTO> getBundleById(@PathVariable Long id) {
        return ResponseEntity.ok(bundleService.getBundleById(id));
    }

    @GetMapping
    public ResponseEntity<List<BundleResponseDTO>> getAllBundles() {
        return ResponseEntity.ok(bundleService.getAllBundles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.noContent().build();
    }
}
//@RestController
//@RequestMapping("/api/v1/bundles")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class BundleController {
//    private final BundleService bundleService;
//
//    @PostMapping
//    public ResponseEntity<Bundle> createBundle(@RequestBody BundleRequestDTO dto) {
//        return ResponseEntity.ok(bundleService.createBundle(dto));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Bundle> updateBundle(@PathVariable Long id, @RequestBody BundleRequestDTO dto) {
//        return ResponseEntity.ok(bundleService.updateBundle(id, dto));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Bundle> getBundle(@PathVariable Long id) {
//        return ResponseEntity.ok(bundleService.getBundleById(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Bundle>> getAllBundles() {
//        return ResponseEntity.ok(bundleService.getAllBundles());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteBundle(@PathVariable Long id) {
//        bundleService.deleteBundle(id);
//        return ResponseEntity.ok("Bundle unit removed from master registry successfully.");
//    }
//
////    @PostMapping public ResponseEntity<Bundle> create(@RequestBody BundleRequestDTO dto) { return ResponseEntity.ok(bundleService.createBundle(dto)); }
////    @GetMapping public ResponseEntity<List<Bundle>> getAll() { return ResponseEntity.ok(bundleService.getAllBundles()); }
//
//}
