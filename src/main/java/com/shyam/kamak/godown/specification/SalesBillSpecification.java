package com.shyam.kamak.godown.specification;

import com.shyam.kamak.godown.model.SalesBill;
import com.shyam.kamak.godown.model.Customer;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalesBillSpecification {

    public static Specification<SalesBill> getDynamicSearchCriteria(
            String globalSearch, String id, String billNumber, String financialYear, String customerName, String grandTotal) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Target Column Filters (AND logic block)
            if (id != null && !id.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
            }
            if (billNumber != null && !billNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), "%" + billNumber.toLowerCase() + "%"));
            }
            if (financialYear != null && !financialYear.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), "%" + financialYear.toLowerCase() + "%"));
            }

            // CRITICAL JOIN: Dynamic SQL Join to filter by customer attributes
            if (customerName != null && !customerName.trim().isEmpty()) {
                Join<SalesBill, Customer> customerJoin = root.join("customer");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + customerName.toLowerCase() + "%"));
            }

            // Safe BigDecimal parsing for numeric column inputs
            if (grandTotal != null && !grandTotal.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("grandTotal"), new BigDecimal(grandTotal.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction()); // Fail gracefully if non-numbers are entered
                }
            }

            // 2. Global Text Input Cross-Cutting Search Override (OR logic block)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.toLowerCase() + "%";
                Join<SalesBill, Customer> customerJoin = root.join("customer");

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), cleanSearch)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

