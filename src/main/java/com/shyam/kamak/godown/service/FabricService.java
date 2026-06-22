package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.FabricRequestDTO;
import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.mapper.FabricMapper;
import com.shyam.kamak.godown.model.Fabric;
import com.shyam.kamak.godown.repository.FabricRepository;
import com.shyam.kamak.godown.service.FabricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FabricService {

    private final FabricRepository fabricRepository;
    private final FabricMapper fabricMapper;

    @Transactional
    public FabricResponseDTO createFabric(FabricRequestDTO request) {
        validateFabricUniqueness(request.getName(), request.getWidth(), null);

        Fabric fabric = fabricMapper.toEntity(request);
        return fabricMapper.toResponseDto(fabricRepository.save(fabric));
    }

    @Transactional
    public FabricResponseDTO updateFabric(Long id, FabricRequestDTO request) {
        Fabric existingFabric = fabricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + id));

        validateFabricUniqueness(request.getName(), request.getWidth(), id);

        fabricMapper.updateEntityFromDto(request, existingFabric);
        return fabricMapper.toResponseDto(fabricRepository.save(existingFabric));
    }

    private void validateFabricUniqueness(String name, BigDecimal width, Long currentId) {
        fabricRepository.findByNameAndWidth(name, width)
                .ifPresent(fabric -> {
                    // Fail if another distinct record already owns this Name + Width identity
                    if (currentId == null || !fabric.getId().equals(currentId)) {
                        throw new IllegalStateException(
                                String.format("Fabric with name '%s' and width '%s' already exists.", name, width)
                        );
                    }
                });
    }

    @Transactional(readOnly = true)
    public FabricResponseDTO getFabricById(Long id) {
        return fabricRepository.findById(id)
                .map(fabricMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<FabricResponseDTO> getAllFabrics() {
        return fabricRepository.findAll().stream()
                .map(fabricMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteFabric(Long id) {
        Fabric fabric = fabricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabric not found with id: " + id));
        fabricRepository.delete(fabric);
    }
}
//import com.shyam.kamak.godown.dto.FabricDTO;
//import com.shyam.kamak.godown.mapper.FabricMapper;
//import com.shyam.kamak.godown.model.Fabric;
//import com.shyam.kamak.godown.repository.FabricRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class FabricService {
//
//    private final FabricRepository fabricRepository;
//    private final FabricMapper fabricMapper;
//
//    @Transactional
//    public FabricDTO createFabric(FabricDTO dto) {
//        // Validate name + width unique criteria programmatically to avoid raw DB exceptions
//        if (fabricRepository.findAll().stream().anyMatch(f ->
//                f.getName().equalsIgnoreCase(dto.name()) &&
//                        f.getWidthInches().compareTo(dto.widthInches()) == 0)) {
//            throw new IllegalStateException("A fabric with name '" + dto.name() +
//                    "' and width " + dto.widthInches() + "\" already exists.");
//        }
//
//        var fabric = fabricMapper.toEntity(dto);
//        return fabricMapper.toDTO(fabricRepository.save(fabric));
//    }
//
//    @Transactional
//    public FabricDTO updateFabric(Long id, FabricDTO dto) {
//        Fabric fabric = fabricRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Fabric profile not found with ID: " + id));
//
//        // Check if updating name/width conflicts with another existing fabric profile
//        boolean duplicateExists = fabricRepository.findAll().stream().anyMatch(f ->
//                !f.getId().equals(id) &&
//                        f.getName().equalsIgnoreCase(dto.name()) &&
//                        f.getWidthInches().compareTo(dto.widthInches()) == 0);
//
//        if (duplicateExists) {
//            throw new IllegalStateException("Cannot update. Another fabric with name '" + dto.name() +
//                    "' and width " + dto.widthInches() + "\" already exists.");
//        }
//
//        fabricMapper.updateEntityFromDto(dto, fabric);
//        return fabricMapper.toDTO(fabricRepository.save(fabric));
//    }
//
//    @Transactional(readOnly = true)
//    public FabricDTO getFabricById(Long id) {
//        return fabricRepository.findById(id)
//                .map(fabricMapper::toDTO)
//                .orElseThrow(() -> new RuntimeException("Fabric profile not found with ID: " + id));
//    }
//
//    @Transactional(readOnly = true)
//    public List<FabricDTO> getAllFabrics() {
//        return fabricRepository.findAll().stream()
//                .map(fabricMapper::toDTO)
//                .toList();
//    }
//
//    @Transactional
//    public void deleteFabric(Long id) {
//        Fabric fabric = fabricRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Fabric profile not found with ID: " + id));
//
//        // Note: Database-level foreign key constraints protect against accidental deletion
//        // if this fabric id is actively used by existing bundle_items rows.
//        fabricRepository.delete(fabric);
//    }
//


//    @Transactional
//    public FabricDTO createFabric(FabricDTO dto) {
//        Fabric fabric = new Fabric();
//        fabric.setName(dto.getName());
//        fabric.setWidthInches(dto.getWidthInches());
//        fabric.setCurrentPricePerMeter(dto.getCurrentPricePerMeter());
//        Fabric saved = fabricRepository.save(fabric);
//        dto.setId(saved.getId());
//        return dto;
//    }
//
//    @Transactional(readOnly = true)
//    public List<FabricDTO> getAllFabrics() {
//        return fabricRepository.findAll().stream().map(f -> {
//            FabricDTO d = new FabricDTO();
//            d.setId(f.getId()); d.setName(f.getName()); d.setWidthInches(f.getWidthInches()); d.setCurrentPricePerMeter(f.getCurrentPricePerMeter());
//            return d;
//        }).collect(Collectors.toList());
//    }
//}
