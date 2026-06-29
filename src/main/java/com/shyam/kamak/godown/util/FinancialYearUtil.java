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


    public static String deriveFinancialYearToken(LocalDate targetDate) {
        if (targetDate == null) {
            return "";
        }
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        // Indian Financial Year runs from April (Month 4) to March (Month 3)
        int startYear = (month >= 4) ? year : year - 1;
        String startYearShort = String.valueOf(startYear).substring(2);
        String endYearShort = String.valueOf(startYear + 1).substring(2);

        return "FY" + startYearShort + "-" + endYearShort; // Returns clean tokens like "FY26-27"
    }
}
