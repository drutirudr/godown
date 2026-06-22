package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BundleResponseDTO {
    private Long id;
    private String bundleNumber;
    private String financialYear;
    private String manufacturerCode;
    private boolean sold;
    private List<BundleItemResponseDTO> items;
    private BigDecimal totalBundleValue;
}
//import java.util.List;

//public record BundleRequestDTO(
//        String manufacturerCode,
//        List<BundleItemRequestDTO> items
//) {}
