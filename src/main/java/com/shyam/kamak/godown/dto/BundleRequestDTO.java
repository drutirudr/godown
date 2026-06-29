package com.shyam.kamak.godown.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BundleRequestDTO {

    @NotNull(message = "Bundle date is required")
    private LocalDate bundleDate;

    @NotBlank(message = "Manufacturer code is required")
    private String manufacturerCode;

    @NotEmpty(message = "Bundle must contain at least one fabric item")
    @Valid
    private List<BundleItemRequestDTO> items;
}
//import java.util.List;

//public record BundleRequestDTO(
//        String manufacturerCode,
//        List<BundleItemRequestDTO> items
//) {}
