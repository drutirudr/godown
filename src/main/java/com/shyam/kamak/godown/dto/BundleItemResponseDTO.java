package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BundleItemResponseDTO {
    private Long id;
    private Long fabricId;
    private String fabricName;
    private BigDecimal fabricWidth;
    private BigDecimal fabricCurrentCostPerMeter;
    private String color;
    private Integer numberOfRolls;
    private BigDecimal metersPerRoll;
    private BigDecimal frozenCostPerMeter;
    private BigDecimal itemTotalValue;
}
