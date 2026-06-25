package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.model.TypeOfBill;
import com.shyam.kamak.godown.dto.TypeOfBillRequestDTO;
import com.shyam.kamak.godown.dto.TypeOfBillResponseDTO;
import com.shyam.kamak.godown.exception.ResourceNotFoundException;
import com.shyam.kamak.godown.repository.TypeOfBillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeOfBillService {

    private final TypeOfBillRepository typeOfBillRepository;

    @Transactional
    public TypeOfBillResponseDTO createTypeOfBill(TypeOfBillRequestDTO request) {
        if (typeOfBillRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new IllegalArgumentException("A billing category already exists matching code constraint: " + request.getCode());
        }

        TypeOfBill entity = TypeOfBill.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .groupType(request.getGroupType())
                .build();

        return mapToResponse(typeOfBillRepository.save(entity));
    }

    @Transactional
    public TypeOfBillResponseDTO updateTypeOfBill(Long id, TypeOfBillRequestDTO request) {
        TypeOfBill existing = typeOfBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill Type record not found with id: " + id));

        typeOfBillRepository.findByCodeIgnoreCase(request.getCode())
                .ifPresent(matched -> {
                    if (!matched.getId().equals(id)) {
                        throw new IllegalArgumentException("Code value '" + request.getCode() + "' is already occupied by another category configuration.");
                    }
                });

        existing.setName(request.getName().trim());
        existing.setCode(request.getCode().trim().toUpperCase());
        existing.setGroupType(request.getGroupType());

        return mapToResponse(typeOfBillRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public TypeOfBillResponseDTO getTypeOfBillById(Long id) {
        return typeOfBillRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("TypeOfBill code profile row not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TypeOfBillResponseDTO> getAllTypeOfBills() {
        return typeOfBillRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteTypeOfBill(Long id) {
        if (!typeOfBillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete non-existent category record reference mapping index: " + id);
        }
        typeOfBillRepository.deleteById(id);
    }

    // Manual mapping layer fallback to decouple code without extra MapStruct configuration modifications
    private TypeOfBillResponseDTO mapToResponse(TypeOfBill entity) {
        TypeOfBillResponseDTO dto = new TypeOfBillResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setGroupType(entity.getGroupType());
        return dto;
    }
}

