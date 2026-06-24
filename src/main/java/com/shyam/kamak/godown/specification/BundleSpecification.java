package com.shyam.kamak.godown.specification;

import com.shyam.kamak.godown.model.Bundle;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BundleSpecification {

    public static Specification<Bundle> getDynamicSearchCriteria(
            String globalSearch, String id, String bundleNumber, String financialYear, String manufacturerCode, String sold) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Column Specific Targets (AND mapping logic)
            if (id != null && !id.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
            }
            if (bundleNumber != null && !bundleNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bundleNumber")), "%" + bundleNumber.toLowerCase() + "%"));
            }
            if (financialYear != null && !financialYear.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), "%" + financialYear.toLowerCase() + "%"));
            }
            if (manufacturerCode != null && !manufacturerCode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturerCode")), "%" + manufacturerCode.toLowerCase() + "%"));
            }
            if (sold != null && !sold.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("sold"), Boolean.parseBoolean(sold.trim())));
            }

            // 2. Global Text Overrides Cross-Search (OR logic block)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bundleNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("manufacturerCode")), cleanSearch)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

