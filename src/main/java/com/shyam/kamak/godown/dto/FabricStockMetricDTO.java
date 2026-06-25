package com.shyam.kamak.godown.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FabricStockMetricDTO {
    private Long fabricId;
    private String fabricName;
    private Double fabricWidth;
    private Long rollsSold;
    private BigDecimal metersSold;
    private Long rollsAvailable;
    private BigDecimal metersAvailable;
}
