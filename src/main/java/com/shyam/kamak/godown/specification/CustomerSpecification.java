package com.shyam.kamak.godown.specification;


import com.shyam.kamak.godown.model.Customer;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static Specification<Customer> getDynamicSearchCriteria(
            String globalSearch, String id, String name, String contactNumber,
            String address, String city, String state, String pincode) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Target Column Filters (AND logic block)
            if (id != null && !id.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
                } catch (NumberFormatException e) {
                    // Fail gracefully if a user types text inside a numerical filter box
                    predicates.add(criteriaBuilder.disjunction());
                }
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (contactNumber != null && !contactNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("contactNumber"), "%" + contactNumber + "%"));
            }
            if (address != null && !address.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
            }
            if (city != null && !city.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (state != null && !state.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), "%" + state.toLowerCase() + "%"));
            }
            if (pincode != null && !pincode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("pincode"), "%" + pincode + "%"));
            }

            // 2. Global Text Input Cross-Cutting Search Override (OR logic block)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.toLowerCase() + "%";
                Predicate globalOrBlock = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("contactNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("pincode")), cleanSearch)
                );
                predicates.add(globalOrBlock);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

