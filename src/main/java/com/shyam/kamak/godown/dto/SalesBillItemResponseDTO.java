package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesBillItemResponseDTO {
    private Long id;
    private Long bundleId;
    private String bundleNumber;
    private String bundleFinancialYear;
    private Integer totalRolls;                 // Aggregate rolls for the row
    private BigDecimal totalMeters;             // Aggregate meters for the row
    private BigDecimal subtotal;                // Aggregate value for the row
    private List<SalesBillItemDetailDTO> details; // Breaks down specific internal fabrics for UI table auto-grouping
}