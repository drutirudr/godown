package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.BundleBatchRequestDTO;
import com.shyam.kamak.godown.dto.BundleRequestDTO;
import com.shyam.kamak.godown.dto.BundleResponseDTO;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.service.BundleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bundles")
@RequiredArgsConstructor
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

    @GetMapping("/all")
    public ResponseEntity<List<BundleResponseDTO>> getAllBundles() {
        return ResponseEntity.ok(bundleService.getAllBundles());
    }

    @GetMapping("/allAvailable")
    public ResponseEntity<List<BundleResponseDTO>> getAllBundlesAvailable() {
        return ResponseEntity.ok(bundleService.getAllBundlesAvailable());
    }

    @GetMapping("/search-available")
    public ResponseEntity<List<BundleResponseDTO>> getSearchAvailableBundles(@RequestParam String query) {
        return ResponseEntity.ok(bundleService.getSearchAvailableBundles(query));
    }

    /**
     * 🚀 FIXED ENDPOINT FOR INFINITE SCROLL AG GRID
     * Completely removes old, direct specification calls that caused compilation failures.
     * Integrates directly with the multi-tab rolling data horizon architecture.
     */
    @GetMapping("/paged")
    public ResponseEntity<Slice<BundleResponseDTO>> getPagedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "AVAILABLE") String tabViewMode,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String bundleNumber,
            @RequestParam(required = false) String manufacturerCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Slice handles millions of records smoothly by omitting the COUNT(*) query
        Slice<BundleResponseDTO> resultSlice = bundleService.searchBundlesPartitioned(
                tabViewMode, search, id, bundleNumber, manufacturerCode, startDate, endDate, pageable
        );

        return ResponseEntity.ok(resultSlice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/next-number")
    public ResponseEntity<String> getNextAvailableBundleNumber(@RequestParam String date) {
        // 🚀 Reads cleanly from the tracker table without applying any row locks
        String previewNumber = bundleService.getPreviewSequenceNumber(date);
        return ResponseEntity.ok(previewNumber);
    }

    @PostMapping(path = "/batch-lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Bundle>> getBatchAvailableByCodes(@RequestBody BundleBatchRequestDTO request) {
        List<Bundle> bundles = bundleService.getBatchAvailableByCodes(request);
        return ResponseEntity.ok(bundles);
    }

}