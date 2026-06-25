package com.shyam.kamak.godown.dto;

// src/main/java/com/textile/control/dto/TypeOfBillRequestDTO.java
import com.shyam.kamak.godown.model.TypeOfBill.BillGroupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TypeOfBillRequestDTO {

    @NotBlank(message = "Billing classification name is mandatory")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Unique category code identifier is mandatory")
    @Size(max = 30, message = "Code cannot exceed 30 characters")
    private String code;

    @NotNull(message = "Product base structural group type is mandatory")
    private BillGroupType groupType; // FABRIC, YARN, or GRANULES
}

