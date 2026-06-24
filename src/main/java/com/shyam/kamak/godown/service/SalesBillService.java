package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.dto.SalesBillRequestDTO;
import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.SalesBillMapper;
import com.shyam.kamak.godown.model.*;
import com.shyam.kamak.godown.repository.BundleRepository;
import com.shyam.kamak.godown.repository.CustomerRepository;
import com.shyam.kamak.godown.repository.SalesBillRepository;
import com.shyam.kamak.godown.util.FinancialYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SalesBillService {

    private final SalesBillRepository salesBillRepository;
    private final BundleRepository bundleRepository;
    private final CustomerRepository customerRepository;
    private final SalesBillMapper salesBillMapper;

    @Transactional
    public SalesBillResponseDTO createBill(SalesBillRequestDTO request) {
        String currentFY = FinancialYearUtil.getCurrentFinancialYear();
        String billNumber = String.format("%05d", salesBillRepository.findMaxBillNumberByFinancialYear(currentFY) + 1);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer record not found."));

        SalesBill salesBill = SalesBill.builder()
                .billNumber(billNumber)
                .financialYear(currentFY)
                .customer(customer)
                .discountType(request.getDiscountType())
                .discountRate(request.getDiscountRate())
                .taxType(request.getTaxType())
                .taxRate(request.getTaxRate())
                .build();

        processAndCalculateBill(salesBill, request.getBundleNumbers());
        return salesBillMapper.toResponseDto(salesBillRepository.save(salesBill));
    }

    @Transactional
    public SalesBillResponseDTO updateBill(Long id, SalesBillRequestDTO request) {
        SalesBill existingBill = salesBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Bill not found with id: " + id));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));

        existingBill.getItems().forEach(item -> item.getBundle().setSold(false));
        existingBill.getItems().clear();

        existingBill.setCustomer(customer);
        existingBill.setDiscountType(request.getDiscountType());
        existingBill.setDiscountRate(request.getDiscountRate());
        existingBill.setTaxType(request.getTaxType());
        existingBill.setTaxRate(request.getTaxRate());

        processAndCalculateBill(existingBill, request.getBundleNumbers());
        return salesBillMapper.toResponseDto(salesBillRepository.save(existingBill));
    }

    private void processAndCalculateBill(SalesBill salesBill, Set<String> bundleNumbers) {
        BigDecimal runningSubtotal = BigDecimal.ZERO;

        for (String combinedBarcode : bundleNumbers) {
            String[] parts = combinedBarcode.trim().split("-(?=[^-]*$)");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid barcode mapping format. Expected: FYXX-XX-NNNNN");
            }
            String financialYear = parts[0];
            String bundleNumber = parts[1];

            Bundle bundle = bundleRepository.findByBundleNumberAndFinancialYear(bundleNumber, financialYear)
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle missing: " + combinedBarcode));

            if (bundle.isSold() && !isBundleLinkedToThisBill(salesBill, bundle)) {
                throw new IllegalStateException("Bundle " + combinedBarcode + " is already attached to another invoice.");
            }

            bundle.setSold(true);

            // 1. Calculate row aggregates for rolls and meters natively using Java streams
            int bundleTotalRolls = bundle.getItems().stream()
                    .mapToInt(item -> item.getNumberOfRolls())
                    .sum();

            BigDecimal bundleTotalMeters = bundle.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2. Compute dynamic line value utilizing frozen cost configurations
            BigDecimal bundleTotalValue = bundle.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getNumberOfRolls())
                            .multiply(item.getMetersPerRoll())
                            .multiply(item.getFrozenCostPerMeter()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            SalesBillItem billItem = SalesBillItem.builder()
                    .bundle(bundle)
                    .totalRolls(bundleTotalRolls)       // Saved directly to the database
                    .totalMeters(bundleTotalMeters)     // Saved directly to the database
                    .subtotal(bundleTotalValue)
                    .build();

            salesBill.addItem(billItem);
            runningSubtotal = runningSubtotal.add(bundleTotalValue);
        }

        salesBill.setSubtotalAmount(runningSubtotal);

        // Calculate discount deductions
        BigDecimal discountAmount = salesBill.getDiscountType() == SalesBill.CalculationType.PERCENT
                ? runningSubtotal.multiply(salesBill.getDiscountRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : salesBill.getDiscountRate();
        salesBill.setDiscountAmount(discountAmount);

        BigDecimal netAfterDiscount = runningSubtotal.subtract(discountAmount);
        if (netAfterDiscount.compareTo(BigDecimal.ZERO) < 0) netAfterDiscount = BigDecimal.ZERO;

        // Calculate tax additions
        BigDecimal taxAmount = salesBill.getTaxType() == SalesBill.CalculationType.PERCENT
                ? netAfterDiscount.multiply(salesBill.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : salesBill.getTaxRate();
        salesBill.setTaxAmount(taxAmount);

        salesBill.setGrandTotal(netAfterDiscount.add(taxAmount));
    }

    private boolean isBundleLinkedToThisBill(SalesBill bill, Bundle bundle) {
        if (bill.getId() == null) return false;
        return bill.getItems().stream().anyMatch(item -> item.getBundle().getId().equals(bundle.getId()));
    }


    @Transactional(readOnly = true)
    public SalesBillResponseDTO getBillByCombinedSearch(String combinedSearch) {
        if (!combinedSearch.startsWith("BILL-")) {
            throw new IllegalArgumentException("Search string identifier must commence with 'BILL-' prefix layout.");
        }
        String scope = combinedSearch.substring(5);
        String[] parts = scope.split("-(?=[^-]*$)");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid layout format configuration segments.");

        // FIX: Added correct array indexing [0] and [1]
        String billNumber = parts[0];
        String financialYear = parts[1];

        return salesBillRepository.findByBillNumberAndFinancialYear(billNumber, financialYear)
                .map(salesBillMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice bill not found: " + combinedSearch));
    }

    @Transactional(readOnly = true) public SalesBillResponseDTO getBillById(Long id) { return salesBillRepository.findById(id).map(salesBillMapper::toResponseDto).orElseThrow(() -> new ResourceNotFoundException("Bill not found")); }
    @Transactional(readOnly = true) public List<SalesBillResponseDTO> getAllBills() { return salesBillRepository.findAll().stream().map(salesBillMapper::toResponseDto).toList(); }
    @Transactional public void deleteBill(Long id) { SalesBill bill = salesBillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found")); bill.getItems().forEach(item -> item.getBundle().setSold(false)); salesBillRepository.delete(bill); }


    @Transactional(readOnly = true)
    public Page<SalesBillResponseDTO> getAllBillsPaged(Specification<SalesBill> spec, Pageable pageable) {
        return salesBillRepository.findAll(spec, pageable).map(salesBillMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public SalesBillResponseDTO previewBill(SalesBillRequestDTO request) {
        // Mock a provisional bill identifier for display purposes
        String provisionalFY = FinancialYearUtil.getCurrentFinancialYear();

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer context missing for preview."));

        SalesBill previewBill = SalesBill.builder()
                .billNumber("PREVIEW")
                .financialYear(provisionalFY)
                .customer(customer)
                .discountType(request.getDiscountType())
                .discountRate(request.getDiscountRate())
                .taxType(request.getTaxType())
                .taxRate(request.getTaxRate())
                .build();

        // Architectural Win: Execute calculation logic without saving or altering bundle "isSold" state in DB
        calculatePreviewTotals(previewBill, request.getBundleNumbers());

        return salesBillMapper.toResponseDto(previewBill);
    }

    private void calculatePreviewTotals(SalesBill salesBill, Set<String> bundleNumbers) {
        BigDecimal runningSubtotal = BigDecimal.ZERO;

        for (String combinedBarcode : bundleNumbers) {
            String[] parts = combinedBarcode.trim().split("-(?=[^-]*$)");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid barcode format. Expected: FYXX-XX-NNNNN");
            }
            String financialYear = parts[0];
            String bundleNumber = parts[1];

            Bundle bundle = bundleRepository.findByBundleNumberAndFinancialYear(bundleNumber, financialYear)
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle missing: " + combinedBarcode));

            // CRITICAL ARCHITECTURAL DIFFERENCE:
            // We omit the bundle.setSold(true) check here because this is a non-binding mock preview calculation.

            int bundleTotalRolls = bundle.getItems().stream().mapToInt(BundleItem::getNumberOfRolls).sum();
            BigDecimal bundleTotalMeters = bundle.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal bundleTotalValue = bundle.getItems().stream()
                    .map(item -> BigDecimal.valueOf(item.getNumberOfRolls())
                            .multiply(item.getMetersPerRoll())
                            .multiply(item.getFrozenCostPerMeter()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            SalesBillItem billItem = SalesBillItem.builder()
                    .bundle(bundle)
                    .totalRolls(bundleTotalRolls)
                    .totalMeters(bundleTotalMeters)
                    .subtotal(bundleTotalValue)
                    .build();

            salesBill.addItem(billItem);
            runningSubtotal = runningSubtotal.add(bundleTotalValue);
        }

        salesBill.setSubtotalAmount(runningSubtotal);

        // Discount Engine Math
        BigDecimal discountAmount = salesBill.getDiscountType() == SalesBill.CalculationType.PERCENT
                ? runningSubtotal.multiply(salesBill.getDiscountRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : salesBill.getDiscountRate();
        salesBill.setDiscountAmount(discountAmount);

        BigDecimal netAfterDiscount = runningSubtotal.subtract(discountAmount);
        if (netAfterDiscount.compareTo(BigDecimal.ZERO) < 0) netAfterDiscount = BigDecimal.ZERO;

        // Tax Engine Math
        BigDecimal taxAmount = salesBill.getTaxType() == SalesBill.CalculationType.PERCENT
                ? netAfterDiscount.multiply(salesBill.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : salesBill.getTaxRate();
        salesBill.setTaxAmount(taxAmount);

        salesBill.setGrandTotal(netAfterDiscount.add(taxAmount));
    }
}
//import com.shyam.kamak.godown.dto.SalesBillRequestDTO;
//import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
//import com.shyam.kamak.godown.mapper.SalesBillMapper;
//import com.shyam.kamak.godown.model.*;
//import com.shyam.kamak.godown.repository.*;
//import com.shyam.kamak.godown.util.Utils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import lombok.extern.slf4j.Slf4j;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SalesBillService {
//    private final SalesBillRepository salesBillRepository;
//    private final CustomerRepository customerRepository;
//    private final BundleRepository bundleRepository;
//    private final SequenceGeneratorService sequenceGeneratorService;
//    private final SalesBillMapper salesBillMapper;
//
//    @Transactional
//    public SalesBill createSalesBill(SalesBillRequestDTO dto) {
//        Customer customer = customerRepository.findById(dto.customerId())
//                .orElseThrow(() -> new RuntimeException("Customer records match missing."));
//
//        SalesBill bill = new SalesBill();
//        String fy = Utils.getCurrentFinancialYear();
//        Integer nextSeq = sequenceGeneratorService.getNextSequence("BILL", fy);
//
//        bill.setFinancialYear(fy);
//        bill.setBusinessBillNumber("INV-" + fy + "-" + String.format("%05d", nextSeq));
//        bill.setBillSequenceNumber(nextSeq);
//        bill.setCustomer(customer);
//
//        calculateAndPopulateBillDetails(bill, dto);
//        return salesBillRepository.save(bill);
//    }
//
//    @Transactional
//    public SalesBill updateSalesBill(Long billId, SalesBillRequestDTO dto) {
//        SalesBill existingBill = salesBillRepository.findById(billId)
//                .orElseThrow(() -> new RuntimeException("Invoice summary not found"));
//
//        // FIX 1: Explicitly fetch and isolate bundles to avoid collection mutation exceptions
//        List<Bundle> bundlesToRelease = new ArrayList<>();
//        for (SalesBillItem item : existingBill.getSalesBillItems()) {
//            Bundle b = item.getBundle();
//            b.setStatus(BundleStatus.AVAILABLE);
//            bundlesToRelease.add(b);
//
//            // FIX: Explicitly sever the relationship to prevent memory orphan leaks
//            item.setSalesBill(null);
//            item.setBundle(null);
//        }
//        bundleRepository.saveAll(bundlesToRelease);
//
//        // Clear and flush orphan rows to the database immediately before re-populating
//        existingBill.getSalesBillItems().clear();
//        //salesBillRepository.saveAndFlush(existingBill);
//        // CRITICAL FIX: Flush the deletion and state updates to MySQL immediately.
//        // This clears the dirty cache state, allowing previously selected bundles to be safely re-evaluated.
//        salesBillRepository.saveAndFlush(existingBill);
//        bundleRepository.flush();
//
//        // Step 3: Recompute calculations with safety safeguards
//        calculateAndPopulateBillDetails(existingBill, dto);
//
//        return salesBillRepository.save(existingBill);
//    }
//
//    private void calculateAndPopulateBillDetails(SalesBill bill, SalesBillRequestDTO dto) {
//        if (dto.bundleIds() == null || dto.bundleIds().isEmpty()) {
//            throw new IllegalArgumentException("Batch processing aborted: The requested billing bundle list cannot be null or empty.");
//        }
//
//        List<Long> distinctBundleIds = dto.bundleIds().stream()
//                .distinct()
//                .collect(Collectors.toList());
//        List<Bundle> bundles = bundleRepository.findAllById(distinctBundleIds);
//        if(bundles.size() != distinctBundleIds.size()) {
//            throw new IllegalArgumentException("Batch processing aborted: One or more bundle records do not exist in inventory registry.");
//        }
//
//        List<SalesBillItem> billItems = new ArrayList<>();
//        BigDecimal subTotal = BigDecimal.ZERO;
//
//        for (Bundle bundle : bundles) {
////            // Allow update states if it's already bound to THIS bill, block if owned by another SOLD entry
////            if (bundle.getStatus() == BundleStatus.SOLD && !isBundleLinkedToBill(bill, bundle.getId())) {
////                throw new IllegalStateException("Process Failed: Bundle " + bundle.getBusinessBundleId() + " is currently locked into another invoice transaction.");
////            }
//            // Check availability - safe because our update workflow has already cleared and flushed old configurations
//            if (bundle.getStatus() == BundleStatus.SOLD) {
//                throw new IllegalStateException("Process Failed: Bundle " + bundle.getBusinessBundleId() + " is currently locked into another invoice transaction.");
//            }
//
//            BigDecimal totalMeters = BigDecimal.ZERO;
//            BigDecimal bundleSubTotal = BigDecimal.ZERO;
//
//            for (BundleItem item : bundle.getBundleItems()) {
//                BigDecimal itemMeters = BigDecimal.valueOf(item.getNumRolls()).multiply(item.getMetersPerRoll());
//                BigDecimal itemCost = itemMeters.multiply(item.getSnapshotPricePerMeter());
//
//                totalMeters = totalMeters.add(itemMeters);
//                bundleSubTotal = bundleSubTotal.add(itemCost);
//            }
//
//            SalesBillItem billItem = new SalesBillItem();
//            billItem.setSalesBill(bill);
//            billItem.setBundle(bundle);
//            billItem.setSnapshotTotalMeters(totalMeters);
//            billItem.setSnapshotBundleSubtotal(bundleSubTotal);
//            billItems.add(billItem);
//
//            subTotal = subTotal.add(bundleSubTotal);
//            bundle.setStatus(BundleStatus.SOLD);
//        }
//
//        bill.setSalesBillItems(billItems);
//        bill.setSubTotal(subTotal);
//
//        // FIX 3: Fixed non-terminating decimal precision expansion holes by adding scale explicit contexts
////        bill.setDiscountType(dto.getDiscountType());
////        // Safe conversion from native double to precise BigDecimal instance
////        BigDecimal inputDiscountRate = BigDecimal.valueOf(dto.getDiscountRate() != null ? dto.getDiscountRate() : 0.0);
////        bill.setDiscountRate(inputDiscountRate);
////
////        BigDecimal discountAmt = BigDecimal.ZERO;
////        if (dto.getDiscountType() == DiscountType.FLAT) {
////            discountAmt = inputDiscountRate;
////        } else if (dto.getDiscountType() == DiscountType.PERCENTAGE) {
////            discountAmt = subTotal.multiply(inputDiscountRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
////        }
////        bill.setDiscountAmount(discountAmt.setScale(2, RoundingMode.HALF_UP));
////
////        BigDecimal taxableBase = subTotal.subtract(bill.getDiscountAmount());
////        bill.setTaxType(dto.getTaxType());
////
////        BigDecimal inputTaxRate = BigDecimal.valueOf(dto.getTaxRatePercent() != null ? dto.getTaxRatePercent() : 0.0);
////        bill.setTaxRatePercent(inputTaxRate);
////
////        BigDecimal taxAmt = taxableBase.multiply(dto.getTaxRatePercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
////        bill.setTaxAmount(taxAmt.setScale(2, RoundingMode.HALF_UP));
////
////        bill.setGrandTotal(taxableBase.add(bill.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
//        bill.setDiscountType(dto.discountType());
//        BigDecimal inputDiscountRate = BigDecimal.valueOf(dto.discountRate() != null ? dto.discountRate() : 0.0);
//        bill.setDiscountRate(inputDiscountRate.setScale(2, RoundingMode.HALF_UP));
//
//        BigDecimal discountAmt = BigDecimal.ZERO;
//        if (dto.discountType() == DiscountType.FLAT) {
//            discountAmt = inputDiscountRate;
//        } else if (dto.discountType() == DiscountType.PERCENTAGE) {
//            // Compute with an intermediate high precision scale of 4, then cleanly downcast to 2
//            discountAmt = subTotal.multiply(inputDiscountRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//        }
//        // CRITICAL FIX: Set scale on an isolated instance before invoking entity state mutations
//        BigDecimal finalDiscountAmount = discountAmt.setScale(2, RoundingMode.HALF_UP);
//        bill.setDiscountAmount(finalDiscountAmount);
//
//        BigDecimal taxableBase = subTotal.subtract(finalDiscountAmount);
//        bill.setTaxType(dto.taxType());
// 
//        BigDecimal inputTaxRate = BigDecimal.valueOf(dto.taxRatePercent() != null ? dto.taxRatePercent() : 0.0);
//        bill.setTaxRatePercent(inputTaxRate.setScale(2, RoundingMode.HALF_UP));
//
//        BigDecimal taxAmt = taxableBase.multiply(inputTaxRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//        BigDecimal finalTaxAmount = taxAmt.setScale(2, RoundingMode.HALF_UP);
//        bill.setTaxAmount(finalTaxAmount);
//
//        // Final mathematical aggregation step
//        BigDecimal finalGrandTotal = taxableBase.add(finalTaxAmount).setScale(2, RoundingMode.HALF_UP);
//        bill.setGrandTotal(finalGrandTotal);
//    }
//
//
//    @Transactional
//    public void deleteSalesBill(Long id) {
//        SalesBill bill = salesBillRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Invoice record not found"));
//
//        // FIX 2: Release and save stock entities before initiating parent record sweeps
//        List<Bundle> bundlesToRelease = new ArrayList<>();
//        for (SalesBillItem item : bill.getSalesBillItems()) {
//            Bundle bundle = item.getBundle();
//            bundle.setStatus(BundleStatus.AVAILABLE);
//            bundlesToRelease.add(bundle);
//        }
//        bundleRepository.saveAll(bundlesToRelease);
//        bundleRepository.flush();
//
//        salesBillRepository.delete(bill);
//    }
//
//    @Transactional(readOnly = true)
//    public SalesBill getBillById(Long id) { return salesBillRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice missing.")); }
//
//    @Transactional(readOnly = true)
//    public List<SalesBill> getAllBills() { return salesBillRepository.findAll(); }
//
//    @Transactional
//    public SalesBillResponseDTO createSalesBillDTO(SalesBillRequestDTO dto) {
//        return salesBillMapper.toDTO(createSalesBill(dto));
//    }
//
//    @Transactional
//    public SalesBillResponseDTO updateSalesBillDTO(Long billId, SalesBillRequestDTO dto) {
//        return salesBillMapper.toDTO(updateSalesBill(billId, dto));
//    }
//
//    @Transactional(readOnly = true)
//    public SalesBillResponseDTO getBillByIdDTO(Long id) {
//        return salesBillMapper.toDTO(getBillById(id));
//    }
//
////    @Transactional
////    public SalesBill createSalesBill(SalesBillRequestDTO dto) {
////        Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer records missing."));
////        SalesBill bill = new SalesBill();
////        String fy = FinancialYearUtil.getCurrentFinancialYear();
////        Integer nextSeq = sequenceGeneratorService.getNextSequence("BILL", fy);
////
////        bill.setFinancialYear(fy);
////        bill.setBusinessBillNumber("INV-" + fy + "-" + String.format("%05d", nextSeq));
////        bill.setBillSequenceNumber(nextSeq);
////        bill.setCustomer(customer);
////
////        calculateAndPopulateBillDetails(bill, dto);
////        return salesBillRepository.save(bill);
////    }
////
////    @Transactional
////    public SalesBill updateSalesBill(Long billId, SalesBillRequestDTO dto) {
////        SalesBill existingBill = salesBillRepository.findById(billId).orElseThrow(() -> new RuntimeException("Invoice not found"));
////
////        List<Bundle> bundlesToRelease = new ArrayList<>();
////        for (SalesBillItem item : existingBill.getSalesBillItems()) {
////            Bundle b = item.getBundle();
////            b.setStatus(BundleStatus.AVAILABLE);
////            bundlesToRelease.add(b);
////            item.setSalesBill(null);
////            item.setBundle(null);
////        }
////        bundleRepository.saveAll(bundlesToRelease);
////        existingBill.getSalesBillItems().clear();
////
////        salesBillRepository.saveAndFlush(existingBill);
////        bundleRepository.flush();
////
////        calculateAndPopulateBillDetails(existingBill, dto);
////        existingBill.setUpdatedAt(LocalDateTime.now());
////
////        return salesBillRepository.save(existingBill);
////    }
////
////    private void calculateAndPopulateBillDetails(SalesBill bill, SalesBillRequestDTO dto) {
////        if (dto.getBundleIds() == null || dto.getBundleIds().isEmpty()) { throw new IllegalArgumentException("Requested bundle list cannot be empty."); }
////        List<Long> distinctBundleIds = dto.getBundleIds().stream().distinct().collect(Collectors.toList());
////        List<Bundle> bundles = bundleRepository.findAllById(distinctBundleIds);
////        if(bundles.size() != distinctBundleIds.size()) { throw new IllegalArgumentException("One or more bundles do not exist."); }
////
////        List<SalesBillItem> billItems = new ArrayList<>();
////        BigDecimal subTotal = BigDecimal.ZERO;
////
////        for (Bundle bundle : bundles) {
////            if (bundle.getStatus() == BundleStatus.SOLD) { throw new IllegalStateException("Bundle " + bundle.getBusinessBundleId() + " is already sold."); }
////
////            BigDecimal totalMeters = BigDecimal.ZERO;
////            BigDecimal bundleSubTotal = BigDecimal.ZERO;
////
////            for (BundleItem item : bundle.getBundleItems()) {
////                BigDecimal itemMeters = BigDecimal.valueOf(item.getNumRolls()).multiply(item.getMetersPerRoll());
////                BigDecimal itemCost = itemMeters.multiply(item.getSnapshotPricePerMeter());
////                totalMeters = totalMeters.add(itemMeters);
////                bundleSubTotal = bundleSubTotal.add(itemCost);
////            }
////
////            SalesBillItem billItem = new SalesBillItem();
////            billItem.setSalesBill(bill); billItem.setBundle(bundle); billItem.setSnapshotTotalMeters(totalMeters); billItem.setSnapshotBundleSubtotal(bundleSubTotal);
////            billItems.add(billItem);
////            subTotal = subTotal.add(bundleSubTotal);
////            bundle.setStatus(BundleStatus.SOLD);
////        }
////
////        bill.setSalesBillItems(billItems);
////        bill.setSubTotal(subTotal);
////
////        bill.setDiscountType(dto.getDiscountType());
////        BigDecimal inputDiscountRate = BigDecimal.valueOf(dto.getDiscountRate() != null ? dto.getDiscountRate() : 0.0);
////        bill.setDiscountRate(inputDiscountRate.setScale(2, RoundingMode.HALF_UP));
////
////        BigDecimal discountAmt = BigDecimal.ZERO;
////        if (dto.getDiscountType() == DiscountType.FLAT) { discountAmt = inputDiscountRate; }
////        else if (dto.getDiscountType() == DiscountType.PERCENTAGE) { discountAmt = subTotal.multiply(inputDiscountRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); }
////        BigDecimal finalDiscountAmount = discountAmt.setScale(2, RoundingMode.HALF_UP);
////        bill.setDiscountAmount(finalDiscountAmount);
////
////        BigDecimal taxableBase = subTotal.subtract(finalDiscountAmount);
////        bill.setTaxType(dto.getTaxType());
////        BigDecimal inputTaxRate = BigDecimal.valueOf(dto.getTaxRatePercent() != null ? dto.getTaxRatePercent() : 0.0);
////        bill.setTaxRatePercent(inputTaxRate.setScale(2, RoundingMode.HALF_UP));
////
////        BigDecimal taxAmt = taxableBase.multiply(inputTaxRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
////        BigDecimal finalTaxAmount = taxAmt.setScale(2, RoundingMode.HALF_UP);
////        bill.setTaxAmount(finalTaxAmount);
////
////        bill.setGrandTotal(taxableBase.add(finalTaxAmount).setScale(2, RoundingMode.HALF_UP));
////    }
////
////    @Transactional
////    public void deleteSalesBill(Long id) {
////        SalesBill bill = salesBillRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
////        List<Bundle> bundlesToRelease = new ArrayList<>();
////        for (SalesBillItem item : bill.getSalesBillItems()) {
////            Bundle bundle = item.getBundle(); bundle.setStatus(BundleStatus.AVAILABLE); bundlesToRelease.add(bundle);
////        }
////        bundleRepository.saveAll(bundlesToRelease);
////        bundleRepository.flush();
////        salesBillRepository.delete(bill);
////    }
////
////    @Transactional(readOnly = true) public SalesBill getBillById(Long id) { return salesBillRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice missing.")); }
////    @Transactional(readOnly = true) public List<SalesBill> getAllBills() { return salesBillRepository.findAll(); }
//
//}
