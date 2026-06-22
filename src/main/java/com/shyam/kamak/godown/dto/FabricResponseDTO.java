package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FabricResponseDTO {
    private Long id;
    private String name;
    private BigDecimal width;
    private BigDecimal currentCostPerMeter;
    private Long version;

    // Optional: Include these to expose audit data to the React UI
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}