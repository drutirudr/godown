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
//import com.shyam.kamak.godown.dto.BundleItemRequestDTO;
//import com.shyam.kamak.godown.dto.BundleRequestDTO;
//import com.shyam.kamak.godown.model.Bundle;
//import com.shyam.kamak.godown.model.BundleItem;
//import org.mapstruct.*;
//
//@Mapper(componentModel = "spring")
//public interface BundleMapper {
//
//    @Mapping(source = "items", target = "bundleItems")
//    Bundle toEntity(BundleRequestDTO dto);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    @Mapping(source = "items", target = "bundleItems")
//    void updateEntityFromDto(BundleRequestDTO dto, @MappingTarget Bundle bundle);
//
//    // 2. Explicitly map child elements so IDs are matched and retained
//    void updateItemFromDto(BundleItemRequestDTO dto, @MappingTarget BundleItem item);
//
//    @AfterMapping
//    default void linkChildToParent(BundleRequestDTO dto, @MappingTarget Bundle bundle) {
//        if (bundle.getBundleItems() != null) {
//            bundle.getBundleItems().forEach(item -> item.setBundle(bundle));
//        }
//    }
//}
//
