package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.TypeOfBillRequestDTO;
import com.shyam.kamak.godown.dto.TypeOfBillResponseDTO;
import com.shyam.kamak.godown.service.TypeOfBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/type-of-bills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permits clean dev communication access lines with React
public class TypeOfBillController {

    private final TypeOfBillService typeOfBillService;

    @PostMapping
    public ResponseEntity<TypeOfBillResponseDTO> createBillType(@Valid @RequestBody TypeOfBillRequestDTO request) {
        return new ResponseEntity<>(typeOfBillService.createTypeOfBill(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeOfBillResponseDTO> updateBillType(
            @PathVariable Long id,
            @Valid @RequestBody TypeOfBillRequestDTO request) {
        return ResponseEntity.ok(typeOfBillService.updateTypeOfBill(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeOfBillResponseDTO> getBillTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(typeOfBillService.getTypeOfBillById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TypeOfBillResponseDTO>> getAllBillTypes() {
        return ResponseEntity.ok(typeOfBillService.getAllTypeOfBills());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBillType(@PathVariable Long id) {
        typeOfBillService.deleteTypeOfBill(id);
        return ResponseEntity.noContent().build();
    }
}

