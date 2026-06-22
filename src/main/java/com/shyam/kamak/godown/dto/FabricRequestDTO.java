package com.shyam.kamak.godown.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FabricRequestDTO {
    @NotBlank(message = "Fabric name is required")
    private String name;

    @NotNull(message = "Fabric width is required")
    @DecimalMin(value = "0.1", message = "Width must be greater than 0")
    private BigDecimal width;

    @NotNull(message = "Current cost per meter is required")
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    private BigDecimal currentCostPerMeter;
}