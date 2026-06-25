package com.shyam.kamak.godown.dto;

import com.shyam.kamak.godown.model.SalesBill;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class SalesBillRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "TypeOfBill Reference Identifier is required")
    private Long typeOfBillId; // Links directly to the new master database table row ID

    @NotNull(message = "Bill Date is required")
    private LocalDate billDate;

    private String lrNumber;
    private String lrDate;
    private String transporterName;
    private String vehicleNumber;
    private String ewayBillNumber;
    private String eInvoiceNumber;

    @NotNull(message = "Discount type is required")
    private SalesBill.CalculationType discountType;

    @NotNull(message = "Discount rate is required")
    @DecimalMin(value = "0.0", message = "Discount rate cannot be negative")
    private BigDecimal discountRate;

    @NotNull(message = "Tax type is required")
    private SalesBill.CalculationType taxType;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate;

    @NotEmpty(message = "At least one bundle identification label is required")
    private Set<String> bundleNumbers;
}