package com.shyam.kamak.godown.specification;

import com.shyam.kamak.godown.model.Fabric;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class FabricSpecification {

    public static Specification<Fabric> getDynamicSearchCriteria(
            String globalSearch, String id, String name, String width, String currentCostPerMeter) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Column Specific Targets (AND mapping logic)
            if (id != null && !id.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (width != null && !width.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("width"), new BigDecimal(width.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }
            if (currentCostPerMeter != null && !currentCostPerMeter.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("currentCostPerMeter"), new BigDecimal(currentCostPerMeter.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }

            // 2. Global Text Overrides Cross-Search (OR logic block)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch)
                ));
            }

            // Fixed the array initialization syntax here
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
