package com.shyam.kamak.godown.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDTO {
    private Long totalInvoicesIssued;
    private BigDecimal grossRevenueCollected;
    private BigDecimal averageInvoiceValue;
    private Long activeClientAccountsCount;
    private Long totalBundlesProduced;
    private Long bundlesSoldInventoryCount;
    private Long stockAvailableInventoryCount;

    // Analytical Distribution Layout Matrix Data Models
    private Map<String, Long> productCategoryDistributionMatrix; // Grouped by FABRIC/YARN/GRANULES
    private List<TopCustomerMetric> topPerformingCustomers;
    //private List<FabricInventoryMetric> highestMovingFabrics;

    private List<TopCustomerMetricDTO> topCustomers;
    private List<FabricStockMetricDTO> fabricStockLedger;

    @Data
    @AllArgsConstructor
    public static class TopCustomerMetric {
        private String customerName;
        private Long orderCount;
        private BigDecimal totalSpent;
    }

    @Data
    @AllArgsConstructor
    public static class FabricInventoryMetric {
        private String fabricName;
        private Long totalRollsSold;
        private Double totalMetersSold;
    }
}