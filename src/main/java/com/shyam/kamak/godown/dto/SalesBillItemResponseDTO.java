package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesBillItemResponseDTO {
    private Long id;
    private Long bundleId;
    private String bundleNumber;
    private Integer totalRolls;
    private BigDecimal totalMeters;
    private BigDecimal subtotal;
    private List<SalesBillItemDetailDTO> details;
}