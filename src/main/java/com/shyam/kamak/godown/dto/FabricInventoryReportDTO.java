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
public class FabricInventoryReportDTO {
    private Long fabricId;
    private String fabricName;
    private Double fabricWidth;

    // 📊 Rolls Tracking
    private Long totalRollsSold;
    private Long totalRollsAvailable;

    // 📊 Length Metric Tracking
    private BigDecimal totalMetersSold;
    private BigDecimal totalMetersAvailable;
}
