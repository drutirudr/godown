package com.shyam.kamak.godown.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BundleItemRequestDTO {

    // Nullable for new entries, provided for updating existing records
    private Long id;

    @NotNull(message = "Fabric ID is required")
    private Long fabricId;

    @NotBlank(message = "Color is required for this item")
    private String color;

    @NotNull(message = "Number of rolls is required")
    @Min(value = 1, message = "Rolls must be greater than 0")
    private Integer numberOfRolls;

    @NotNull(message = "Meters per roll is required")
    @Min(value = 1, message = "Meters must be greater than 0")
    private BigDecimal metersPerRoll;
}
//import java.math.BigDecimal;
//
//public record BundleItemRequestDTO(
//        Long fabricId,
//        Integer numRolls,
//        BigDecimal metersPerRoll,
//        String color
//) {}
