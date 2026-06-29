package com.shyam.kamak.godown.specification;

import com.shyam.kamak.godown.model.SalesBill;
import com.shyam.kamak.godown.model.Customer;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.shyam.kamak.godown.model.TypeOfBill;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


public class SalesBillSpecification {

    public static Specification<SalesBill> getDynamicSearchCriteria(
            String globalSearch,
            String id,
            String billNumber,
            String customerName,
            String grandTotal,
            LocalDate startDate,     // ⚡ CHANGED TYPE TO LOCALDATE MATCHING YOUR ENGINE
            LocalDate endDate,       // ⚡ CHANGED TYPE TO LOCALDATE MATCHING YOUR ENGINE
            String lrNumber,
            String transporterName,
            String vehicleNumber,
            String typeOfBillName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Precise Column Targets (AND Blocks)
            if (id != null && !id.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
            }
            if (billNumber != null && !billNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), "%" + billNumber.trim().toLowerCase() + "%"));
            }

            // Customer Profile Join Filter
            if (customerName != null && !customerName.trim().isEmpty()) {
                Join<SalesBill, Customer> customerJoin = root.join("customer");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + customerName.trim().toLowerCase() + "%"));
            }

            // Master Billing Type Join Filter
            if (typeOfBillName != null && !typeOfBillName.trim().isEmpty()) {
                Join<SalesBill, TypeOfBill> typeJoin = root.join("typeOfBill");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("name")), "%" + typeOfBillName.trim().toLowerCase() + "%"));
            }

            if (lrNumber != null && !lrNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lrNumber")), "%" + lrNumber.trim().toLowerCase() + "%"));
            }
            if (transporterName != null && !transporterName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("transporterName")), "%" + transporterName.trim().toLowerCase() + "%"));
            }
            if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleNumber")), "%" + vehicleNumber.trim().toLowerCase() + "%"));
            }

            // 🚀 INDEX-FRIENDLY CRITERIA BOUNDARY CHECKS USING INJECTED LOCALDATE
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("billDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("billDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("billDate"), endDate));
            }

            if (grandTotal != null && !grandTotal.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("grandTotal"), new BigDecimal(grandTotal.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }

            // 2. Cross-Cutting Single-Field Global Text Search (OR Blocks)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.trim().toLowerCase() + "%";

                Join<SalesBill, Customer> customerJoin = root.join("customer");
                Join<SalesBill, TypeOfBill> typeJoin = root.join("typeOfBill");

                List<Predicate> orPredicates = new ArrayList<>();
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("name")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lrNumber")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("transporterName")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleNumber")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("ewayBillNumber")), cleanSearch));
                orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("eInvoiceNumber")), cleanSearch));

                // Dynamically fallback query to locate targeted ranges if input looks like a year label
                Predicate globalFyPredicate = buildFinancialYearPredicate(globalSearch.trim(), root, criteriaBuilder);
                if (globalFyPredicate != null) orPredicates.add(globalFyPredicate);

                predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildFinancialYearPredicate(String input, jakarta.persistence.criteria.Root<SalesBill> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        try {
            String cleanInput = input.toUpperCase().replace("FY", "").replace(" ", "").trim();
            int startYear;

            if (cleanInput.contains("-")) {
                String[] parts = cleanInput.split("-");
                startYear = Integer.parseInt(parts[0].trim());
                if (startYear < 100) startYear += (startYear < 70) ? 2000 : 1900;
            } else {
                startYear = Integer.parseInt(cleanInput);
                if (startYear < 100) startYear += (startYear < 70) ? 2000 : 1900;
            }

            LocalDate startRange = LocalDate.of(startYear, 4, 1);
            LocalDate endRange = LocalDate.of(startYear + 1, 3, 31);
            return cb.between(root.get("billDate"), startRange, endRange);
        } catch (Exception e) {
            return null;
        }
    }
}