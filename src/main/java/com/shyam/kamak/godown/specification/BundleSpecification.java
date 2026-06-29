package com.shyam.kamak.godown.specification;

import com.shyam.kamak.godown.model.Bundle;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BundleSpecification {

    public static Specification<Bundle> getDynamicSearchCriteria(
            String globalSearch,
            String id,
            String bundleNumber,
            String manufacturerCode,
            Boolean sold,            // ⚡ MUST BE BOOLEAN
            LocalDate startDate,     // ⚡ MUST BE LOCALDATE
            LocalDate endDate) {     // ⚡ MUST BE LOCALDATE

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Column Specific Targets (AND Logic)
            if (id != null && !id.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
            }
            if (bundleNumber != null && !bundleNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bundleNumber")), "%" + bundleNumber.trim().toLowerCase() + "%"));
            }
            if (manufacturerCode != null && !manufacturerCode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturerCode")), "%" + manufacturerCode.trim().toLowerCase() + "%"));
            }
            if (sold != null) {
                predicates.add(criteriaBuilder.equal(root.get("sold"), sold));
            }

            // 2. Date Horizon Range Filter Predicates
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("bundleDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("bundleDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("bundleDate"), endDate));
            }

            // 3. Global Text Cross-Cutting Search Override (OR Logic)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.trim().toLowerCase() + "%";
                List<Predicate> orPredicates = new ArrayList<>();

                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bundleNumber")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturerCode")), cleanSearch));

                // Dynamically process historical financial year inputs inside global searches
                Predicate globalFyPredicate = buildFinancialYearPredicate(globalSearch.trim(), root, criteriaBuilder);
                if (globalFyPredicate != null) {
                    orPredicates.add(globalFyPredicate);
                }

                predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildFinancialYearPredicate(String input, jakarta.persistence.criteria.Root<Bundle> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        try {
            String cleanInput = input.toUpperCase().replace("FY", "").replace(" ", "").trim();
            int startYear;
            if (cleanInput.contains("-")) {
                startYear = Integer.parseInt(cleanInput.split("-")[0].trim());
                if (startYear < 100) startYear += (startYear < 70) ? 2000 : 1900;
            } else {
                startYear = Integer.parseInt(cleanInput);
                if (startYear < 100) startYear += (startYear < 70) ? 2000 : 1900;
            }
            return cb.between(root.get("bundleDate"), LocalDate.of(startYear, 4, 1), LocalDate.of(startYear + 1, 3, 31));
        } catch (Exception e) {
            return null;
        }
    }
}