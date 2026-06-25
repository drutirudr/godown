package com.shyam.kamak.godown.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.shyam.kamak.godown.model.SalesBill;

@Data
public class SalesBillResponseDTO {
    private Long id;
    private String billNumber;
    private String financialYear;
    private LocalDate billDate;
    private Long customerId;
    private String customerName;

    // ➕ EXPANDED MASTER TABLE LOOKUP ATTRIBUTES FOR UI VIEWS
    private Long typeOfBillId;
    private String typeOfBillName;
    private String typeOfBillCode;
    private String typeOfBillGroup; // Returns FABRIC / YARN / GRANULES safely

    private String lrNumber;
    private String lrDate;
    private String transporterName;
    private String vehicleNumber;
    private String ewayBillNumber;
    private String eInvoiceNumber;

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