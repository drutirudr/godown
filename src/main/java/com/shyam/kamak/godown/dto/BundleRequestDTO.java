package com.shyam.kamak.godown.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BundleRequestDTO {
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
