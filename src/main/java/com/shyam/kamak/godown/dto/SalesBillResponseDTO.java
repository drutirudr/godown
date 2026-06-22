package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.shyam.kamak.godown.model.SalesBill;

@Data
public class SalesBillResponseDTO {
    private Long id;
    private String billNumber;
    private String financialYear;
    private Long customerId;
    private String customerName;
    private BigDecimal subtotalAmount;
    private SalesBill.CalculationType discountType;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private SalesBill.CalculationType taxType;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private List<SalesBillItemResponseDTO> items;
    private LocalDateTime createdAt;
    private String createdBy;
}
//public record SalesBillResponseDTO(
//        Long id,
//        String businessBillNumber,
//        String financialYear,
//        Instant billDate,
//        Instant createdAt,
//        Instant updatedAt,
//        String createdBy,
//        String updatedBy,
//        String customerName,
//        BigDecimal subTotal,
//        DiscountType discountType,
//        BigDecimal discountAmount,
//        TaxType taxType,
//        BigDecimal taxAmount,
//        BigDecimal grandTotal,
//        List<BillItemDetailsDTO> items
//) {
//    public static record BillItemDetailsDTO(
//            String businessBundleId,
//            BigDecimal snapshotTotalMeters,
//            BigDecimal snapshotBundleSubtotal
//    ) {}
//}
