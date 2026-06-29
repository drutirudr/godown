package com.shyam.kamak.godown.mapper;

import com.shyam.kamak.godown.dto.BundleItemResponseDTO;
import com.shyam.kamak.godown.dto.BundleResponseDTO;
import com.shyam.kamak.godown.model.Bundle;
import com.shyam.kamak.godown.model.BundleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BundleMapper {

    // Added explicit target for financialYear to point to your transient entity getter method
    @Mapping(target = "financialYear", source = "financialYear")
    @Mapping(target = "bundleDate", source = "bundleDate")
    @Mapping(target = "totalBundleValue", source = "items", qualifiedByName = "calculateTotalBundleValue")
    BundleResponseDTO toResponseDto(Bundle bundle);

    @Mapping(target = "fabricId", source = "fabric.id")
    @Mapping(target = "fabricName", source = "fabric.name")
    @Mapping(target = "fabricWidth", source = "fabric.width")
    @Mapping(target = "itemTotalValue", source = "bundleItem", qualifiedByName = "calculateItemTotal")
    BundleItemResponseDTO toItemResponseDto(BundleItem bundleItem);

    @Named("calculateItemTotal")
    default BigDecimal calculateItemTotal(BundleItem item) {
        if (item == null) return BigDecimal.ZERO;
        BigDecimal totalMeters = BigDecimal.valueOf(item.getNumberOfRolls()).multiply(item.getMetersPerRoll());
        return totalMeters.multiply(item.getFrozenCostPerMeter());
    }

    @Named("calculateTotalBundleValue")
    default BigDecimal calculateTotalBundleValue(List<BundleItem> items) {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}