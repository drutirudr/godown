package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.BundleItemRequestDTO;
import com.shyam.kamak.godown.dto.BundleRequestDTO;
import com.shyam.kamak.godown.dto.BundleResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.BundleMapper;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.model.BundleItem;
import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.model.GlobalSequence;
import com.shyam.kamak.godown.repository.BundleRepository;
import com.shyam.kamak.godown.repository.FabricRepository;
import com.shyam.kamak.godown.repository.GlobalSequenceRepository;
import com.shyam.kamak.godown.specification.BundleSpecification;
import com.shyam.kamak.godown.util.FinancialYearUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleService {

    private final BundleRepository bundleRepository;
    private final FabricRepository fabricRepository;
    private final BundleMapper bundleMapper;
    private final GlobalSequenceRepository globalSequenceRepository;

    @Value("${godown.data-horizon.years:5}")
    private int dataHorizonYears;

    @Value("${godown.data-horizon.archive-years:2}")
    private int archiveHorizonYears;

    // =========================================================================
    // 🚀 SECTION 1: SEQUENTIAL BUNDLE NUMBER TRACKER ACTIONS (NON-BLOCKING)
    // =========================================================================

    @Transactional(readOnly = true)
    public String getPreviewSequenceNumber(String dateStr) {
        LocalDate targetDate = Optional.ofNullable(parseLocalDateSafely(dateStr)).orElseGet(LocalDate::now);
        String currentFY = deriveFinancialYearToken(targetDate);

        // Read running metrics safely without any row blocking or pessimistic locks
        int nextNumber = globalSequenceRepository.findById("BUNDLE")
                .map(seq -> seq.getFinancialYear().equalsIgnoreCase(currentFY) ? seq.getRunningNumber() + 1 : 1)
                .orElse(1);

        //return formatBundleNumberString(targetDate, nextNumber);
        return "BUN-" + currentFY + "-" + nextNumber;
    }

    @Transactional
    public String generateNextSequentialBundleNumber(LocalDate targetDate) {
        String currentFY = deriveFinancialYearToken(targetDate);

        // 🛡️ Pessimistic FOR UPDATE lock applied cleanly inside a microsecond commit phase
        GlobalSequence seq = globalSequenceRepository.findAndLockByEntityName("BUNDLE")
                .orElseGet(() -> new GlobalSequence("BUNDLE", currentFY, 0));

        if (!seq.getFinancialYear().equalsIgnoreCase(currentFY)) {
            seq.setFinancialYear(currentFY);
            seq.setRunningNumber(1);
        } else {
            seq.setRunningNumber(seq.getRunningNumber() + 1);
        }
        globalSequenceRepository.save(seq);

        //return formatBundleNumberString(targetDate, seq.getRunningNumber());
        return "BUN-" + currentFY + "-" + seq.getRunningNumber();
    }

    public String getNextAvailableBundleNumber(String dateStr) {
        return getPreviewSequenceNumber(dateStr); // 🚀 Safely routed to non-blocking lookup handler
    }

    // =========================================================================
    // 📦 SECTION 2: CORE CRUD READ/WRITE CORE SERVICE LIFECYCLES
    // =========================================================================

    @Transactional
    public BundleResponseDTO createBundle(BundleRequestDTO request) {
        // Compute and lock sequence code sequentially right during database persistence
        String officialBundleNumber = generateNextSequentialBundleNumber(request.getBundleDate());

        Bundle bundle = Bundle.builder()
                .bundleNumber(officialBundleNumber)
                .bundleDate(request.getBundleDate())
                .manufacturerCode(request.getManufacturerCode())
                .sold(false)
                .build();

        mapAndAttachRequestItems(bundle, request.getItems());
        return bundleMapper.toResponseDto(bundleRepository.save(bundle));
    }

    @Transactional
    public BundleResponseDTO updateBundle(Long id, BundleRequestDTO request) {
        Bundle existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + id));

        validateBundleIsEditable(existingBundle);

        existingBundle.setBundleDate(request.getBundleDate());
        existingBundle.setManufacturerCode(request.getManufacturerCode());

        // Sync and reconcile child collections cleanly
        reconcileBundleItemsCollection(existingBundle, request.getItems());

        return bundleMapper.toResponseDto(bundleRepository.save(existingBundle));
    }

    @Transactional(readOnly = true)
    public BundleResponseDTO getBundleById(Long id) {
        return bundleRepository.findById(id)
                .map(bundleMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BundleResponseDTO> getAllBundles() {
        return bundleRepository.findAll().stream().map(bundleMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponseDTO> getAllBundlesAvailable() {
        return bundleRepository.findBySoldFalse().stream().map(bundleMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponseDTO> getSearchAvailableBundles(String query) {
        Pageable topTenLimit = PageRequest.of(0, 10, Sort.by("bundleNumber").ascending());
        return bundleRepository.findBySoldFalseAndBundleNumberContainingIgnoreCase(query, topTenLimit)
                .getContent().stream().map(bundleMapper::toResponseDto).toList();
    }

    @Transactional
    public void deleteBundle(Long id) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + id));
        validateBundleIsEditable(bundle);
        bundleRepository.delete(bundle);
    }

    @Transactional(readOnly = true)
    public Page<BundleResponseDTO> getAllBundlesPaged(Specification<Bundle> spec, Pageable pageable) {
        return bundleRepository.findAll(spec, pageable).map(bundleMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Slice<BundleResponseDTO> searchBundlesPartitioned(
            String tabViewMode, String search, String id, String bundleNumber, String manufacturerCode,
            String startDateStr, String endDateStr, Pageable pageable) {

        String cleanSearch = cleanStringInput(search);
        String cleanId = cleanStringInput(id);
        String cleanBundleNumber = cleanStringInput(bundleNumber);
        String cleanManufacturerCode = cleanStringInput(manufacturerCode);

        Boolean soldFilter = null;
        LocalDate finalStartDate = parseLocalDateSafely(startDateStr);
        LocalDate finalEndDate = parseLocalDateSafely(endDateStr);

        // Collapse conditional parameters safely
        if ("AVAILABLE".equalsIgnoreCase(tabViewMode)) {
            soldFilter = false;
            if (finalStartDate == null) finalStartDate = LocalDate.now().minusYears(dataHorizonYears);
            if (finalEndDate == null) finalEndDate = LocalDate.now().plusYears(1);
        } else if ("SOLD".equalsIgnoreCase(tabViewMode)) {
            soldFilter = true;
            if (finalStartDate == null) finalStartDate = LocalDate.now().minusYears(dataHorizonYears);
            if (finalEndDate == null) finalEndDate = LocalDate.now().plusDays(1);
        } else if ("ALL".equalsIgnoreCase(tabViewMode)) {
            if (finalStartDate == null && finalEndDate == null && cleanSearch == null) {
                finalStartDate = LocalDate.now().minusYears(archiveHorizonYears);
                finalEndDate = LocalDate.now().plusYears(1);
            }
        }

        if (finalStartDate != null && finalEndDate != null && finalStartDate.isAfter(finalEndDate)) {
            LocalDate temp = finalStartDate;
            finalStartDate = finalEndDate;
            finalEndDate = temp;
        }

        log.info("============== 🚀 CRITERIA PRE-FLIGHT LOG =============");
        log.info("Tab Mode       : {}", tabViewMode);
        log.info("Global Search  : {}", cleanSearch);
        log.info("Start Boundary : {}", finalStartDate);
        log.info("End Boundary   : {}", finalEndDate);
        log.info("========================================================");

        Specification<Bundle> spec = BundleSpecification.getDynamicSearchCriteria(
                cleanSearch, cleanId, cleanBundleNumber, cleanManufacturerCode, soldFilter, finalStartDate, finalEndDate
        );

        return bundleRepository.fetchSliceWithGraph(spec, pageable).map(bundleMapper::toResponseDto);
    }

    // =========================================================================
    // 🛠️ SECTION 3: REUSABLE LOGICAL INTERNAL UTILITY HELPER METHODS
    // =========================================================================

    private String deriveFinancialYearToken(LocalDate targetDate) {
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();
        int startYear = (month >= 4) ? year : year - 1;
        return String.format("FY%02d-%02d", startYear % 100, (startYear + 1) % 100);
    }

//    private String formatBundleNumberString(LocalDate targetDate, int numericId) {
//        String dateToken = String.format("%04d%02d%02d", targetDate.getYear(), targetDate.getMonthValue(), targetDate.getDayOfMonth());
//        return "BUN-" + dateToken + "-" + numericId;
//    }

    private String cleanStringInput(String input) {
        return (input != null && !input.trim().isEmpty()) ? input.trim() : null;
    }

    private void validateBundleIsEditable(Bundle bundle) {
        if (bundle.isSold()) {
            throw new IllegalStateException("Business Rule Violation: Sold bundles are permanently locked from modifications.");
        }
    }

    private void mapAndAttachRequestItems(Bundle bundle, List<BundleItemRequestDTO> dtoList) {
        Map<Long, Fabric> fabricMap = fetchFabricsForRequest(dtoList);
        for (BundleItemRequestDTO dto : dtoList) {
            Fabric fabric = getFabricOrThrow(fabricMap, dto.getFabricId());
            bundle.addItem(buildNewBundleItem(dto, fabric));
        }
    }

    private void reconcileBundleItemsCollection(Bundle bundle, List<BundleItemRequestDTO> incomingDtos) {
        Map<Long, Fabric> fabricMap = fetchFabricsForRequest(incomingDtos);

        Map<String, BundleItem> currentItemsMap = bundle.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getFabric().getId() + "_" + item.getColor().trim().toLowerCase(),
                        item -> item
                ));

        Set<String> incomingSignatures = incomingDtos.stream()
                .map(dto -> dto.getFabricId() + "_" + dto.getColor().trim().toLowerCase())
                .collect(Collectors.toSet());

        // Perform differential item clearing
        List<BundleItem> toRemove = bundle.getItems().stream()
                .filter(item -> !incomingSignatures.contains(item.getFabric().getId() + "_" + item.getColor().trim().toLowerCase()))
                .toList();
        toRemove.forEach(bundle::removeItem);

        // Perform upserts or append new nodes
        for (BundleItemRequestDTO dto : incomingDtos) {
            String signature = dto.getFabricId() + "_" + dto.getColor().trim().toLowerCase();
            Fabric fabric = getFabricOrThrow(fabricMap, dto.getFabricId());

            if (currentItemsMap.containsKey(signature)) {
                BundleItem existingItem = currentItemsMap.get(signature);
                existingItem.setNumberOfRolls(dto.getNumberOfRolls());
                existingItem.setMetersPerRoll(dto.getMetersPerRoll());
                existingItem.setFrozenCostPerMeter(fabric.getCurrentCostPerMeter());
            } else {
                bundle.addItem(buildNewBundleItem(dto, fabric));
            }
        }
    }

    private BundleItem buildNewBundleItem(BundleItemRequestDTO dto, Fabric fabric) {
        return BundleItem.builder()
                .fabric(fabric)
                .color(dto.getColor())
                .numberOfRolls(dto.getNumberOfRolls())
                .metersPerRoll(dto.getMetersPerRoll())
                .frozenCostPerMeter(fabric.getCurrentCostPerMeter())
                .build();
    }

    private Map<Long, Fabric> fetchFabricsForRequest(List<BundleItemRequestDTO> items) {
        Set<Long> fabricIds = items.stream().map(BundleItemRequestDTO::getFabricId).collect(Collectors.toSet());
        return fabricRepository.findAllById(fabricIds).stream()
                .collect(Collectors.toMap(Fabric::getId, fabric -> fabric));
    }

    private Fabric getFabricOrThrow(Map<Long, Fabric> fabricMap, Long fabricId) {
        Fabric fabric = fabricMap.get(fabricId);
        if (fabric == null) {
            throw new ResourceNotFoundException("Fabric record context missing for ID: " + fabricId);
        }
        return fabric;
    }

    private LocalDate parseLocalDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }
}
