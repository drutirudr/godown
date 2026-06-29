package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.SalesBillRequestDTO;
import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
import com.shyam.kamak.godown.service.SalesBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sales-bills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalesBillController {

    private final SalesBillService salesBillService;

    @PostMapping
    public ResponseEntity<SalesBillResponseDTO> createBill(@Valid @RequestBody SalesBillRequestDTO request) {
        return new ResponseEntity<>(salesBillService.createBill(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesBillResponseDTO> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody SalesBillRequestDTO request) {
        return ResponseEntity.ok(salesBillService.updateBill(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesBillResponseDTO> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(salesBillService.getBillById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<SalesBillResponseDTO> getBillByCombinedSearch(@RequestParam("identifier") String identifier) {
        return ResponseEntity.ok(salesBillService.getBillByCombinedSearch(identifier));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SalesBillResponseDTO>> getAllBills() {
        return ResponseEntity.ok(salesBillService.getAllBills());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        salesBillService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview")
    public ResponseEntity<SalesBillResponseDTO> previewBill(@Valid @RequestBody SalesBillRequestDTO request) {
        return ResponseEntity.ok(salesBillService.previewBill(request));
    }

    // 🚀 NEW NON-BLOCKING PREVIEW GATEWAY: Lets operators preview next bill numbers without applying heavy FOR UPDATE locks!
    @GetMapping("/next-number")
    public ResponseEntity<String> getNextAvailableBillNumber(@RequestParam String date) {
        return ResponseEntity.ok(salesBillService.getPreviewSequenceNumber(date));
    }

    // 🚀 REFACTORED TO SLICE WINDOWS: Wipes out full table count scans across millions of rows permanently
    @GetMapping("/paged")
    public ResponseEntity<Slice<SalesBillResponseDTO>> getPagedSalesBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ACTIVE") String tabViewMode, // ACTIVE / HISTORICAL / ALL
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String billNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String grandTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String lrNumber,
            @RequestParam(required = false) String transporterName,
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String typeOfBillName) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Slice<SalesBillResponseDTO> resultSlice = salesBillService.searchSalesBillsPartitioned(
                tabViewMode, search, id, billNumber, customerName, grandTotal,
                startDate, endDate, lrNumber, transporterName, vehicleNumber, typeOfBillName, pageable
        );

        return ResponseEntity.ok(resultSlice);
    }
}
