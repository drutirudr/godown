package com.shyam.kamak.godown.mapper;

import com.shyam.kamak.godown.dto.CustomerRequestDTO;
import com.shyam.kamak.godown.dto.CustomerResponseDTO;
import com.shyam.kamak.godown.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Converts a Customer database entity into a secure Response DTO for the React UI.
     * Inherited audit fields (createdAt, createdBy, etc.) from UserAuditable
     * map automatically because the field names match perfectly.
     */
    CustomerResponseDTO toResponseDto(Customer customer);

    /**
     * Converts an incoming Request DTO into a fresh Customer database entity.
     * Primary keys, versions, and audit trails are explicitly ignored here
     * since they are handled entirely by MySQL and Spring Data JPA Auditing.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Customer toEntity(CustomerRequestDTO dto);

    /**
     * In-place update utility for HTTP PUT requests.
     * Merges incoming modifications from the DTO straight into your existing
     * tracked database entity without changing its primary ID or version state.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(CustomerRequestDTO dto, @MappingTarget Customer customer);
}
//import com.shyam.kamak.godown.dto.CustomerDTO;
//import com.shyam.kamak.godown.model.Customer;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//
//@Mapper(componentModel = "spring")
//public interface CustomerMapper {
//
//    CustomerDTO toDTO(Customer customer);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    Customer toEntity(CustomerDTO dto);
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
//    void updateEntityFromDto(CustomerDTO dto, @MappingTarget Customer customer);
//}