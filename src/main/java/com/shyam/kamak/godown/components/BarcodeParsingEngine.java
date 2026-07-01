package com.shyam.kamak.godown.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class BarcodeParsingEngine {

    @Value("${godown.data.year-pivot-threshold:70}")
    private int yearPivot;

    @Value("${godown.data.century-past-fallback:1900}")
    private int pastCentury;

    @Value("${godown.data.century-future-fallback:2000}")
    private int futureCentury;

    public LocalDate[] getFinancialYearBounds(LocalDate date) {
        if (date == null) return new LocalDate[0];
        int year = date.getYear();
        LocalDate startRange = (date.getMonthValue() < 4) ? LocalDate.of(year - 1, 4, 1) : LocalDate.of(year, 4, 1);
        return new LocalDate[]{startRange, startRange.plusYears(1).minusDays(1)};
    }

    public LocalDate[] extractDatesFromFinancialYearString(String input) {
        try {
            String clean = input.toUpperCase().replace("FY", "").replace(" ", "").trim();
            int startYear = Integer.parseInt(clean.split("-")[1]);

            // 🚀 FIXED: All magic threshold numbers are now safely read from the config properties environment
            if (startYear < 100) {
                startYear += (startYear < yearPivot) ? futureCentury : pastCentury;
            }

            LocalDate startRange = LocalDate.of(startYear, 4, 1);
            return new LocalDate[]{startRange, startRange.plusYears(1).minusDays(1)};
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract financial year dates from value: " + input);
        }
    }

    public String[] splitBarcode(String barcode) {
        if (barcode == null) {
            throw new IllegalArgumentException("Barcode cannot be null.");
        }
        String[] parts = barcode.trim().split("-(?=[^-]*$)");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid barcode target layout: " + barcode);
        }
        return parts;
    }
}