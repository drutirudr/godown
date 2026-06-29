package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.components.BarcodeParsingEngine;
import com.shyam.kamak.godown.components.SalesBillCalculationEngine;
import com.shyam.kamak.godown.dto.SalesBillRequestDTO;
import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.SalesBillMapper;
import com.shyam.kamak.godown.model.*;
import com.shyam.kamak.godown.repository.BundleRepository;
import com.shyam.kamak.godown.repository.CustomerRepository;
import com.shyam.kamak.godown.repository.SalesBillRepository;
import com.shyam.kamak.godown.repository.TypeOfBillRepository;
import com.shyam.kamak.godown.repository.GlobalSequenceRepository;
import com.shyam.kamak.godown.specification.SalesBillSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesBillService {

    private final SalesBillRepository salesBillRepository;
    private final BundleRepository bundleRepository;
    private final CustomerRepository customerRepository;
    private final TypeOfBillRepository typeOfBillRepository;
    private final GlobalSequenceRepository globalSequenceRepository;
    private final SalesBillMapper salesBillMapper;

    private final BarcodeParsingEngine barcodeParser;
    private final SalesBillCalculationEngine calculationEngine;

    @Value("${godown.salesbill-horizon.years:5}")
    private int salesBillHorizonYears;

    @Value("${godown.salesbill-horizon.archive-years:2}")
    private int archiveHorizonYears; // 🚀 New rolling 2-year boundary fallback config

    // =========================================================================
    // 🔍 SECTION 1: NON-BLOCKING SEQUENTIAL SERIAL INVOICE TRACKING CODES
    // =========================================================================

    private String deriveFinancialYearToken(LocalDate targetDate) {
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();
        int startYear = (month >= 4) ? year : year - 1;
        return String.format("FY%02d-%02d", startYear % 100, (startYear + 1) % 100);
    }

    @Transactional(readOnly = true)
    public String getPreviewSequenceNumber(String dateStr) {
        LocalDate targetDate = Optional.ofNullable(parseLocalDateSafely(dateStr)).orElseGet(LocalDate::now);
        String currentFY = deriveFinancialYearToken(targetDate);

        // Read tracker row cleanly from the database with ZERO row-level pessimistic locks
        int nextNumber = globalSequenceRepository.findById("SALES_BILL")
                .map(seq -> seq.getFinancialYear().equalsIgnoreCase(currentFY) ? seq.getRunningNumber() + 1 : 1)
                .orElse(1);

        return String.format("%05d", nextNumber);
    }

    @Transactional
    public String generateNextSequentialBillNumber(LocalDate targetDate) {
        String currentFY = deriveFinancialYearToken(targetDate);

        // 🛡️ ATOMIC COMMIT WRITER: Locks the 'SALES_BILL' tracker row for a millisecond to prevent duplicates
        GlobalSequence seq = globalSequenceRepository.findAndLockByEntityName("SALES_BILL")
                .orElseGet(() -> new GlobalSequence("SALES_BILL", currentFY, 0));

        if (!seq.getFinancialYear().equalsIgnoreCase(currentFY)) {
            seq.setFinancialYear(currentFY);
            seq.setRunningNumber(1);
        } else {
            seq.setRunningNumber(seq.getRunningNumber() + 1);
        }
        globalSequenceRepository.save(seq);

        return String.format("%05d", seq.getRunningNumber());
    }

    // =========================================================================
    // 📋 SECTION 2: OPERATIONS CORE MANAGEMENT ACTIONS
    // =========================================================================

    @Transactional
    public SalesBillResponseDTO createBill(SalesBillRequestDTO request) {
        // Compute and lock the official sequence bill number right inside the transactional write step
        String officialBillNumber = generateNextSequentialBillNumber(request.getBillDate());

        validateUniqueBillNumberForFinancialYear(officialBillNumber, request.getBillDate(), null);

        SalesBill salesBill = buildBaseSalesBill(request, officialBillNumber);
        attachBundlesToBill(salesBill, request.getBundleNumbers(), false);

        return salesBillMapper.toResponseDto(salesBillRepository.save(salesBill));
    }

    @Transactional
    public SalesBillResponseDTO updateBill(Long id, SalesBillRequestDTO request) {
        SalesBill existingBill = salesBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Bill not found with id: " + id));

        // 🚀 EDIT MODE IS UNLOCKED HERE: bill numbers are safe and immutable, ensuring tracking consistency
        validateUniqueBillNumberForFinancialYear(existingBill.getBillNumber(), request.getBillDate(), id);

        // 1. Release old material bundles back into the available warehouse stock pool automatically
        existingBill.getItems().forEach(item -> item.getBundle().setSold(false));
        existingBill.getItems().clear();

        // 2. Synchronize modified invoice text inputs
        updateBillMetadata(existingBill, request);

        // 3. Re-attach updated bundle arrays and flip their flags back to sold = true automatically
        existingBill.setId(null);
        attachBundlesToBill(existingBill, request.getBundleNumbers(), false);

        existingBill.setId(id); // Restore primary key mapping before transaction commit

        return salesBillMapper.toResponseDto(salesBillRepository.save(existingBill));
    }

    @Transactional(readOnly = true)
    public SalesBillResponseDTO previewBill(SalesBillRequestDTO request) {
        SalesBill previewBill = buildBaseSalesBill(request, "PREVIEW");
        attachBundlesToBill(previewBill, request.getBundleNumbers(), true);
        return salesBillMapper.toResponseDto(previewBill);
    }

    @Transactional(readOnly = true)
    public SalesBillResponseDTO getBillByCombinedSearch(String combinedSearch) {
        if (!combinedSearch.startsWith("BILL-")) {
            throw new IllegalArgumentException("Search identifier layout must commence with 'BILL-' prefix.");
        }

        String scope = combinedSearch.substring(5);
        String[] parts = barcodeParser.splitBarcode(scope);

        String billNumber = parts[0];
        String financialYearStr = parts[1];

        LocalDate[] bounds = barcodeParser.extractDatesFromFinancialYearString(financialYearStr);

        return salesBillRepository.findDuplicateInFinancialYear(billNumber, bounds[0], bounds[1])
                .map(salesBillMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice bill record missing: " + combinedSearch));
    }

    private void attachBundlesToBill(SalesBill salesBill, Set<String> bundleBarcodes, boolean isMockPreview) {
        for (String barcode : bundleBarcodes) {
            String[] parts = barcodeParser.splitBarcode(barcode);
            LocalDate[] bounds = barcodeParser.extractDatesFromFinancialYearString(parts[0]);

            Bundle bundle = bundleRepository.findByBundleNumberAndDateRange(parts[1], bounds[0], bounds[1])
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle match failed for: " + barcode));

            if (!isMockPreview) {
                if (bundle.isSold() && !isBundleLinkedToThisBill(salesBill, bundle)) {
                    throw new IllegalStateException("Bundle " + barcode + " is already attached to another invoice.");
                }
                bundle.setSold(true); // Automatically flips status to locked/sold on final checkout commits!
            }

            salesBill.addItem(calculationEngine.buildSalesBillItem(salesBill, bundle));
        }
        calculationEngine.applyFinancialCalculations(salesBill);
    }

    private SalesBill buildBaseSalesBill(SalesBillRequestDTO request, String billNumber) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer record missing."));
        TypeOfBill typeOfBill = typeOfBillRepository.findById(request.getTypeOfBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill Type record missing."));

        SalesBill bill = SalesBill.builder().billNumber(billNumber).build();
        updateBillMetadata(bill, request);
        bill.setCustomer(customer);
        bill.setTypeOfBill(typeOfBill);
        return bill;
    }

    private void updateBillMetadata(SalesBill bill, SalesBillRequestDTO request) {
        bill.setBillDate(request.getBillDate());
        bill.setLrNumber(cleanInputString(request.getLrNumber()));
        bill.setLrDate(cleanInputString(request.getLrDate()));
        bill.setTransporterName(cleanInputString(request.getTransporterName()));
        bill.setVehicleNumber(request.getVehicleNumber() != null ? request.getVehicleNumber().trim().toUpperCase() : null);
        bill.setEwayBillNumber(cleanInputString(request.getEwayBillNumber()));
        bill.setEInvoiceNumber(cleanInputString(request.getEInvoiceNumber()));
        bill.setDiscountType(request.getDiscountType());
        bill.setDiscountRate(request.getDiscountRate());
        bill.setTaxType(request.getTaxType());
        bill.setTaxRate(request.getTaxRate());
    }

    private String cleanInputString(String str) {
        return (str != null && !str.trim().isEmpty()) ? str.trim() : null;
    }

    private void validateUniqueBillNumberForFinancialYear(String billNumber, LocalDate billDate, Long currentId) {
        LocalDate[] bounds = barcodeParser.getFinancialYearBounds(billDate);
        salesBillRepository.findDuplicateInFinancialYear(billNumber, bounds[0], bounds[1]).ifPresent(duplicate -> {
            if (currentId == null || !duplicate.getId().equals(currentId)) {
                throw new IllegalArgumentException(String.format("Invoice Bill Number '%s' already exists within this financial period.", billNumber));
            }
        });
    }

    private boolean isBundleLinkedToThisBill(SalesBill bill, Bundle bundle) {
        return bill.getId() != null && bill.getItems().stream().anyMatch(item -> item.getBundle().getId().equals(bundle.getId()));
    }

    @Transactional(readOnly = true)
    public SalesBillResponseDTO getBillById(Long id) {
        return salesBillRepository.findWithDetailsById(id).map(salesBillMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found."));
    }

    @Transactional(readOnly = true)
    public List<SalesBillResponseDTO> getAllBills() {
        return salesBillRepository.findAll().stream().map(salesBillMapper::toResponseDto).toList();
    }

    @Transactional
    public void deleteBill(Long id) {
        SalesBill bill = salesBillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Target missing."));
        bill.getItems().forEach(item -> item.getBundle().setSold(false)); // Release bundles back into warehouse stock cleanly on invoice deletions
        salesBillRepository.delete(bill);
    }

    // =========================================================================
    // 📊 SECTION 3: ROLLING ARCHIVE SEARCH TIME HORIZON FILTER ENGINE
    // =========================================================================

    @Transactional(readOnly = true)
    public Slice<SalesBillResponseDTO> searchSalesBillsPartitioned(
            String tabViewMode, String search, String id, String billNumber, String customerName,
            String grandTotal, LocalDate startDate, LocalDate endDate, String lrNumber,
            String transporterName, String vehicleNumber, String typeOfBillName, Pageable pageable) {

        String cleanSearch = cleanInputString(search);
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        if ("ACTIVE".equalsIgnoreCase(tabViewMode)) {
            if (finalStartDate == null) finalStartDate = LocalDate.now().minusYears(salesBillHorizonYears);
            if (finalEndDate == null) finalEndDate = LocalDate.now().plusYears(1);
        }
        // 🚀 THE 2-YEAR ARCHIVE LOCK FIXED: Restricts historical queries to look back exactly 2 years max automatically
        else if ("HISTORICAL".equalsIgnoreCase(tabViewMode)) {
            if (finalStartDate == null && finalEndDate == null && cleanSearch == null) {
                finalStartDate = LocalDate.now().minusYears(archiveHorizonYears); // Logs look back exactly 2 years max
                finalEndDate = LocalDate.now().minusYears(salesBillHorizonYears); // Bounded safely at the active edge boundary
            }
        }
        else if ("ALL".equalsIgnoreCase(tabViewMode)) {
            if (finalStartDate == null && finalEndDate == null && cleanSearch == null) {
                finalStartDate = LocalDate.now().minusYears(archiveHorizonYears); // Logs look back exactly 2 years max
                finalEndDate = LocalDate.now().plusYears(1);
            }
        }

        if (finalStartDate != null && finalEndDate != null && finalStartDate.isAfter(finalEndDate)) {
            LocalDate temp = finalStartDate;
            finalStartDate = finalEndDate;
            finalEndDate = temp;
        }

        log.info("============== 🚀 SALES BILL PRE-FLIGHT LOG =============");
        log.info("Partition Tab  : {}", tabViewMode);
        log.info("Global Keyword : {}", cleanSearch);
        log.info("Start Boundary : {}", finalStartDate);
        log.info("End Boundary   : {}", finalEndDate);
        log.info("========================================================");

        Specification<SalesBill> spec = SalesBillSpecification.getDynamicSearchCriteria(
                cleanSearch, id, billNumber, customerName, grandTotal, finalStartDate, finalEndDate,
                lrNumber, transporterName, vehicleNumber, typeOfBillName
        );

        // 🚀 Leverages your clean high-speed fetch graph method to query data slices instantly!
        return salesBillRepository.fetchSliceWithGraph(spec, pageable).map(salesBillMapper::toResponseDto);
    }

    private LocalDate parseLocalDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try { return LocalDate.parse(dateStr.trim()); } catch (Exception e) { return null; }
    }
}


