package com.shyam.kamak.godown.mapper;

import com.shyam.kamak.godown.dto.SalesBillItemDetailDTO;
import com.shyam.kamak.godown.dto.SalesBillItemResponseDTO;
import com.shyam.kamak.godown.dto.SalesBillResponseDTO;
import com.shyam.kamak.godown.model.BundleItem;
import com.shyam.kamak.godown.model.SalesBill;
import com.shyam.kamak.godown.model.SalesBillItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface SalesBillMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "typeOfBillId", source = "typeOfBill.id")
    @Mapping(target = "typeOfBillName", source = "typeOfBill.name")
    @Mapping(target = "typeOfBillCode", source = "typeOfBill.code")
    @Mapping(target = "typeOfBillGroup", source = "typeOfBill.groupType")
    SalesBillResponseDTO toResponseDto(SalesBill salesBill);

    @Mapping(target = "bundleId", source = "bundle.id")
    @Mapping(target = "bundleNumber", source = "bundle.bundleNumber")
    @Mapping(target = "details", source = "bundle.items")
    SalesBillItemResponseDTO toItemResponseDto(SalesBillItem salesBillItem);

    @Mapping(target = "fabricId", source = "fabric.id")
    @Mapping(target = "fabricName", source = "fabric.name")
    @Mapping(target = "fabricWidth", source = "fabric.width")
    @Mapping(target = "totalMeters", source = "bundleItem", qualifiedByName = "calculateMeters")
    @Mapping(target = "totalValue", source = "bundleItem", qualifiedByName = "calculateItemValue")
    SalesBillItemDetailDTO toDetailDto(BundleItem bundleItem);

    @Named("calculateMeters")
    default BigDecimal calculateMeters(BundleItem item) {
        if (item == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll());
    }

    @Named("calculateItemValue")
    default BigDecimal calculateItemValue(BundleItem item) {
        if (item == null) return BigDecimal.ZERO;

        BigDecimal meters = calculateMeters(item);

        // 🚀 STRICT UI RENDERING LOGIC MATCHING BUSINESS RULES:
        // Use the frozen database cost ONLY if the bundle belongs to a finalized, sold invoice record.
        if (item.getBundle() != null && item.getBundle().isSold() && item.getFrozenCostPerMeter() != null) {
            return meters.multiply(item.getFrozenCostPerMeter());
        }

        // 🎯 For unsold bundles, previews, or standalone package inventory sweeps, always read the LIVE cost from the master fabric table
        return meters.multiply(item.getFabric().getCurrentCostPerMeter());
    }
}
