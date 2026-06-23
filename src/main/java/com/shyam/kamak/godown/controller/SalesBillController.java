package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.SalesBillRequestDTO;
import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
import com.shyam.kamak.godown.service.SalesBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
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
}
//@RestController
//@RequestMapping("/api/v1/sales-bills")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class SalesBillController {
//    private final SalesBillService salesBillService;
//
////
//@PostMapping
//public ResponseEntity<SalesBillResponseDTO> createBill(@RequestBody SalesBillRequestDTO dto) {
//    return ResponseEntity.ok(salesBillService.createSalesBillDTO(dto));
//}
//
//    @PutMapping("/{id}")
//    public ResponseEntity<SalesBillResponseDTO> updateBill(@PathVariable Long id, @RequestBody SalesBillRequestDTO dto) {
//        return ResponseEntity.ok(salesBillService.updateSalesBillDTO(id, dto));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<SalesBillResponseDTO> getBill(@PathVariable Long id) {
//        return ResponseEntity.ok(salesBillService.getBillByIdDTO(id));
//    }
//
//
//    @GetMapping
//    public ResponseEntity<List<SalesBill>> getAllBills() {
//        return ResponseEntity.ok(salesBillService.getAllBills());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteBill(@PathVariable Long id) {
//        salesBillService.deleteSalesBill(id);
//        return ResponseEntity.ok("Invoice record successfully removed. Linked stock components restored to stock registry.");
//    }
//
////    @PostMapping public ResponseEntity<SalesBillResponseDTO> createBill(@RequestBody SalesBillRequestDTO dto) { return ResponseEntity.ok(mapToResponseDTO(salesBillService.createSalesBill(dto))); }
////    @PutMapping("/{id}") public ResponseEntity<SalesBillResponseDTO> updateBill(@PathVariable Long id, @RequestBody SalesBillRequestDTO dto) { return ResponseEntity.ok(mapToResponseDTO(salesBillService.updateSalesBill(id, dto))); }
////    @GetMapping("/{id}") public ResponseEntity<SalesBillResponseDTO> getBill(@PathVariable Long id) { return ResponseEntity.ok(mapToResponseDTO(salesBillService.getBillById(id))); }
////    @DeleteMapping("/{id}") public ResponseEntity<String> deleteBill(@PathVariable Long id) { salesBillService.deleteSalesBill(id); return ResponseEntity.ok("Deleted successfully."); }
//
////    private SalesBillResponseDTO mapToResponseDTO(SalesBill bill) {
////        SalesBillResponseDTO res = new SalesBillResponseDTO();
////        res.setId(bill.getId()); res.setBusinessBillNumber(bill.getBusinessBillNumber()); res.setFinancialYear(bill.getFinancialYear()); res.setBillDate(bill.getBillDate()); res.setCustomerName(bill.getCustomer().getCustomerName()); res.setSubTotal(bill.getSubTotal()); res.setDiscountType(bill.getDiscountType()); res.setDiscountAmount(bill.getDiscountAmount()); res.setTaxType(bill.getTaxType()); res.setTaxAmount(bill.getTaxAmount()); res.setGrandTotal(bill.getGrandTotal());
////        res.setItems(bill.getSalesBillItems().stream().map(item -> {
////            SalesBillResponseDTO.BillItemDetailsDTO itemDto = new SalesBillResponseDTO.BillItemDetailsDTO();
////            itemDto.setBusinessBundleId(item.getBundle().getBusinessBundleId());
////            itemDto.setSnapshotTotalMeters(item.getSnapshotTotalMeters());
////            itemDto.setSnapshotBundleSubtotal(item.getSnapshotBundleSubtotal());
////            return itemDto;
////        }).collect(Collectors.toList()));
////        return res;
////    }
//}
//
//
