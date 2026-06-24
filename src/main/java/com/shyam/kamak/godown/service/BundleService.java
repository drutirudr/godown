package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.BundleItemRequestDTO;
import com.shyam.kamak.godown.dto.BundleRequestDTO;
import com.shyam.kamak.godown.dto.BundleResponseDTO;
import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.BundleMapper;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.model.BundleItem;
import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.repository.BundleRepository;
import com.shyam.kamak.godown.repository.FabricRepository;
import com.shyam.kamak.godown.util.FinancialYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BundleService {

    private final BundleRepository bundleRepository;
    private final FabricRepository fabricRepository;
    private final BundleMapper bundleMapper;

    @Transactional
    public BundleResponseDTO createBundle(BundleRequestDTO request) {
        String currentFY = FinancialYearUtil.getCurrentFinancialYear();
        String bundleNumber = generateNextSequentialBundleNumber(currentFY);

        Bundle bundle = Bundle.builder()
                .bundleNumber(bundleNumber)
                .financialYear(currentFY)
                .manufacturerCode(request.getManufacturerCode())
                .sold(false)
                .build();

        Map<Long, Fabric> fabricMap = fetchFabricsForRequest(request.getItems());

        for (BundleItemRequestDTO itemDto : request.getItems()) {
            Fabric fabric = getFabricOrThrow(fabricMap, itemDto.getFabricId());
            bundle.addItem(buildNewBundleItem(itemDto, fabric));
        }

        return bundleMapper.toResponseDto(bundleRepository.save(bundle));
    }

    @Transactional
    public BundleResponseDTO updateBundle(Long id, BundleRequestDTO request) {
        Bundle existingBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + id));

        validateBundleIsEditable(existingBundle);

        // Update root attribute cleanly
        existingBundle.setManufacturerCode(request.getManufacturerCode());

        // Batch load all required fabrics to avoid N+1 queries
        Map<Long, Fabric> fabricMap = fetchFabricsForRequest(request.getItems());

        // --- FIXED SYNCHRONIZATION VIA NATURAL KEY (fabricId + color) ---

        // 1. Map current items using natural business keys for reliable tracking
        Map<String, BundleItem> currentItemsMap = existingBundle.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getFabric().getId() + "_" + item.getColor().trim().toLowerCase(),
                        item -> item
                ));

        // 2. Map incoming payload signatures
        Set<String> incomingSignatures = request.getItems().stream()
                .map(dto -> dto.getFabricId() + "_" + dto.getColor().trim().toLowerCase())
                .collect(Collectors.toSet());

        // 3. Process Removals: Safe cascade drop items missing from the new selection layout
        List<BundleItem> toRemove = existingBundle.getItems().stream()
                .filter(item -> !incomingSignatures.contains(item.getFabric().getId() + "_" + item.getColor().trim().toLowerCase()))
                .toList();

        toRemove.forEach(existingBundle::removeItem);

        // 4. Process Additions or Updates seamlessly without breaking primary IDs
        for (BundleItemRequestDTO dto : request.getItems()) {
            String signature = dto.getFabricId() + "_" + dto.getColor().trim().toLowerCase();
            Fabric fabric = getFabricOrThrow(fabricMap, dto.getFabricId());

            if (currentItemsMap.containsKey(signature)) {
                // UPDATE IN-PLACE: Modifies properties of existing item; database primary ID is completely preserved!
                BundleItem existingItem = currentItemsMap.get(signature);
                existingItem.setNumberOfRolls(dto.getNumberOfRolls());
                existingItem.setMetersPerRoll(dto.getMetersPerRoll());
                existingItem.setFrozenCostPerMeter(fabric.getCurrentCostPerMeter());
            } else {
                // INSERT: This is an entirely fresh line selection combination added to the bundle
                existingBundle.addItem(buildNewBundleItem(dto, fabric));
            }
        }

        return bundleMapper.toResponseDto(bundleRepository.save(existingBundle));
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
        if (!fabricMap.containsKey(fabricId)) {
            throw new ResourceNotFoundException("Fabric record context missing for ID: " + fabricId);
        }
        return fabricMap.get(fabricId);
    }

    private String generateNextSequentialBundleNumber(String financialYear) {
        int nextId = bundleRepository.findMaxBundleNumberByFinancialYear(financialYear) + 1;
        return String.format("%05d", nextId);
    }

    private void validateBundleIsEditable(Bundle bundle) {
        if (bundle.isSold()) {
            throw new IllegalStateException("Business Rule Violation: Sold bundles are permanently locked from modifications.");
        }
    }

    @Transactional(readOnly = true)
    public BundleResponseDTO getBundleById(Long id) {
        return bundleRepository.findById(id)
                .map(bundleMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found with id: " + id));
    }


    @Transactional(readOnly = true)
    public List<BundleResponseDTO> getAllBundles() {
        return bundleRepository.findAll().stream()
                .map(bundleMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponseDTO> getAllBundlesAvailable() {
        return bundleRepository.findBySoldFalse().stream()
                .map(bundleMapper::toResponseDto)
                .toList();
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

    private String calculateFinancialYear() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int month = now.getMonthValue();
        if (month < 4) {
            return "FY" + ((currentYear - 1) % 100) + "-" + (currentYear % 100);
        } else {
            return "FY" + (currentYear % 100) + "-" + ((currentYear + 1) % 100);
        }
    }
}

//import com.shyam.kamak.godown.dto.*;
//import com.shyam.kamak.godown.mapper.BundleMapper;
//import com.shyam.kamak.godown.model.*;
//import com.shyam.kamak.godown.repository.*;
//import com.shyam.kamak.godown.util.Utils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class BundleService {
//    private final BundleRepository bundleRepository;
//    private final FabricRepository fabricRepository;
//    private final SequenceGeneratorService sequenceGeneratorService;
//    private final BundleMapper bundleMapper;
//
////    @Transactional
////    public Bundle createBundle(BundleRequestDTO dto) {
////        Bundle bundle = new Bundle();
////        String fy = FinancialYearUtil.getCurrentFinancialYear();
////
////        // Block Concurrent Overlaps via Lock synchronization logic
//////        synchronized(this) {
//////            Integer maxSeq = bundleRepository.findMaxSequenceByFinancialYear(fy);
//////            bundle.setSequenceNumber(maxSeq + 1);
//////        }
////        String fy = FinancialYearUtil.getCurrentFinancialYear();
////        Integer nextSeq = sequenceGeneratorService.getNextSequence("BUNDLE", fy);
////        //bundle.setSequenceNumber(nextSeq);
////
////        bundle.setFinancialYear(fy);
////
////        //bundle.setBusinessBundleId("BNDL-" + fy + "-" + String.format("%04d", bundle.getSequenceNumber()));
////        int primitiveSequenceValue = nextSeq.intValue(); // Force unboxing to primitive int
////        bundle.setSequenceNumber(primitiveSequenceValue);
////        bundle.setBusinessBundleId("BNDL-" + fy + "-" + String.format("%04d", primitiveSequenceValue));
////
////        bundle.setManufacturerCode(dto.getManufacturerCode());
////        bundle.setStatus(BundleStatus.AVAILABLE);
////
////        for (BundleItemRequestDTO itemDto : dto.getItems()) {
////            Fabric fabric = fabricRepository.findById(itemDto.getFabricId())
////                    .orElseThrow(() -> new RuntimeException("Fabric profile not found"));
////
////            BundleItem item = new BundleItem();
////            item.setFabric(fabric);
////            item.setNumRolls(itemDto.getNumRolls());
////            item.setMetersPerRoll(itemDto.getMetersPerRoll());
////            item.setColor(itemDto.getColor());
////            // CRITICAL AUDIT RULE: Capture current master price right now
////            item.setSnapshotPricePerMeter(fabric.getCurrentPricePerMeter());
////
////            bundle.addBundleItem(item);
////        }
////        return bundleRepository.save(bundle);
////    }
//
////    @Transactional(readOnly = true)
////    public List<Bundle> getAllBundles() { return bundleRepository.findAll(); }
//
//    @Transactional(readOnly = true)
//    public Bundle getBundleById(Long id) {
//        return bundleRepository.findById(id).orElseThrow(() -> new RuntimeException("Bundle not found"));
//    }
//
//    @Transactional
//    public void deleteBundle(Long id) {
//        Bundle bundle = getBundleById(id);
//        if (bundle.getStatus() == BundleStatus.SOLD) {
//            throw new IllegalStateException("Cannot delete a bundle that has already been billed and sold.");
//        }
//        bundleRepository.delete(bundle);
//    }
//
//    @Transactional
//    public Bundle createBundle(BundleRequestDTO dto) {
//        Bundle bundle = new Bundle();
//        String fy = Utils.getCurrentFinancialYear();
//        Integer nextSeq = sequenceGeneratorService.getNextSequence("BUNDLE", fy);
//
//        bundle.setSequenceNumber(nextSeq);
//        bundle.setFinancialYear(fy);
//        bundle.setBusinessBundleId("BNDL-" + fy + "-" + String.format("%04d", nextSeq));
//        bundle.setStatus(BundleStatus.AVAILABLE);
//        bundle.setManufacturerCode(dto.manufacturerCode());
//        // Map simple fields from DTO using mapper
//        //bundleMapper.updateEntityFromDto(dto, bundle);
//
//        // Create and add bundle items with fabric references
//        for (BundleItemRequestDTO itemDto : dto.items()) {
//            Fabric fabric = fabricRepository.findById(itemDto.fabricId()).orElseThrow(() -> new RuntimeException("Fabric profile not found"));
//            BundleItem item = new BundleItem();
//            item.setFabric(fabric);
//            item.setNumRolls(itemDto.numRolls());
//            item.setMetersPerRoll(itemDto.metersPerRoll());
//            item.setColor(itemDto.color());
//            item.setSnapshotPricePerMeter(fabric.getCurrentPricePerMeter());
//            bundle.addBundleItem(item);
//        }
//        return bundleRepository.save(bundle);
//    }
//
//    @Transactional
//    public Bundle updateBundle(Long id, BundleRequestDTO dto) {
//        Bundle bundle = getBundleById(id);
//
//        if (bundle.getStatus() == BundleStatus.SOLD) {
//            throw new IllegalStateException("Cannot update a bundle that has already been billed and sold.");
//        }
//
//        // Let the mapper update simple mutable fields (mapper ignores bundleItems)
//        bundleMapper.updateEntityFromDto(dto, bundle);
//
//        // Replace bundle items: clear existing (orphanRemoval will delete them) and add new ones
//        bundle.getBundleItems().clear();
//
//        for (BundleItemRequestDTO itemDto : dto.items()) {
//            Fabric fabric = fabricRepository.findById(itemDto.fabricId()).orElseThrow(() -> new RuntimeException("Fabric profile not found"));
//            BundleItem item = new BundleItem();
//            item.setFabric(fabric);
//            item.setNumRolls(itemDto.numRolls());
//            item.setMetersPerRoll(itemDto.metersPerRoll());
//            item.setColor(itemDto.color());
//            item.setSnapshotPricePerMeter(fabric.getCurrentPricePerMeter());
//            bundle.addBundleItem(item);
//        }
//
//        return bundleRepository.save(bundle);
//    }
//
//    @Transactional(readOnly = true) public List<Bundle> getAllBundles() { return bundleRepository.findAll(); }
//
//
//}
