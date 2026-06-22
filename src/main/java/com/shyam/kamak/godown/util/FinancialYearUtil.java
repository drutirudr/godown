package com.shyam.kamak.godown.util;

import java.time.LocalDate;

public final class FinancialYearUtil {

    private FinancialYearUtil() {
        // Enforce structural non-instantiability
    }

    public static String getCurrentFinancialYear() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int month = now.getMonthValue();

        // Dynamic evaluation according to standard fiscal criteria (April - March Cycle)
        if (month < 4) {
            return String.format("FY%02d-%02d", (currentYear - 1) % 100, currentYear % 100);
        } else {
            return String.format("FY%02d-%02d", currentYear % 100, (currentYear + 1) % 100);
        }
    }
}
