package com.shyam.kamak.godown.mapper;

import com.shyam.kamak.godown.dto.FabricRequestDTO;
import com.shyam.kamak.godown.dto.FabricResponseDTO;
import com.shyam.kamak.godown.model.Fabric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FabricMapper {

    FabricResponseDTO toResponseDto(Fabric fabric);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Fabric toEntity(FabricRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(FabricRequestDTO dto, @MappingTarget Fabric fabric);
}

