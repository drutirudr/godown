package com.shyam.kamak.godown.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {

        @NotBlank(message = "Customer name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        private String name;

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits")
        private String contactNumber;

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        private String address;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City name cannot exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State name cannot exceed 100 characters")
        private String state;

        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^\\d{6}$", message = "Pincode must be exactly 6 digits")
        private String pincode;
}
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import java.time.Instant;
//
//public record CustomerDTO(
//        Long id,
//
//        @NotBlank(message = "Customer name is mandatory")
//        @Size(max = 100)
//        String customerName,
//
//        @NotBlank(message = "Contact number is mandatory")
//        @Size(max = 15)
//        String contactNumber,
//
//        @NotBlank(message = "Address is mandatory")
//        @Size(max = 255)
//        String address,
//
//        @NotBlank(message = "City is mandatory")
//        @Size(max = 50)
//        String city,
//
//        @NotBlank(message = "State is mandatory")
//        @Size(max = 50)
//        String state,
//
//        @NotBlank(message = "Pincode is mandatory")
//        @Size(max = 10)
//        String pincode,
//
//        // Expose audit timestamps to readers
//        Instant createdAt,
//        Instant updatedAt,
//        String createdBy,
//        String updatedBy
//) {}