//package com.shyam.kamak.godown.dto;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Objects;
//
//public record FabricDTO(
//		Long id,
//		String name,
//		BigDecimal widthInches,
//		BigDecimal currentPricePerMeter,
//
//		// Audit fields
//		Instant createdAt,
//		Instant updatedAt,
//		String createdBy,
//		String updatedBy
//) {
//	public FabricDTO {
//		// defensive copies or basic validation can go here if needed
//	}
//
//	public static FabricDTO of(Long id, String name, BigDecimal widthInches, BigDecimal currentPricePerMeter,
//							   Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
//		return new FabricDTO(id, name, widthInches, currentPricePerMeter, createdAt, updatedAt, createdBy, updatedBy);
//	}
//
//	@Override
//	public String toString() {
//		return "FabricDTO[id=" + id + ", name=" + name + "]";
//	}
//}
