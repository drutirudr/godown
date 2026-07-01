package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesBillItemDetailDTO {
    private Long fabricId;
    private String fabricName;
    private BigDecimal fabricWidth;
    private BigDecimal fabricCurrentCostPerMeter;
    private String color;
    private Integer numberOfRolls;
    private BigDecimal metersPerRoll;
    private BigDecimal frozenCostPerMeter;
    private BigDecimal totalMeters;
    private BigDecimal totalValue;
}
