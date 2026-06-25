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
public class TopCustomerMetricDTO {
    private String customerName;
    private Long totalInvoices;
    private BigDecimal totalContribution;
}