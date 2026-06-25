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
            String financialYear,
            String customerName,
            String grandTotal,
            String billDate,        // ➕ Added target column query filters
            String lrNumber,
            String transporterName,
            String vehicleNumber,
            String typeOfBillName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Column Specific Filtering (AND logic block matching AG Grid column filters)
            if (id != null && !id.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
            }
            if (billNumber != null && !billNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), "%" + billNumber.trim().toLowerCase() + "%"));
            }
            if (financialYear != null && !financialYear.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), "%" + financialYear.trim().toLowerCase() + "%"));
            }

            // JOIN 1: Filter by Customer Profile Attributes
            if (customerName != null && !customerName.trim().isEmpty()) {
                Join<SalesBill, Customer> customerJoin = root.join("customer");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + customerName.trim().toLowerCase() + "%"));
            }

            // JOIN 2: Filter by Custom Relational Bill Category Names
            if (typeOfBillName != null && !typeOfBillName.trim().isEmpty()) {
                Join<SalesBill, TypeOfBill> typeJoin = root.join("typeOfBill");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("name")), "%" + typeOfBillName.trim().toLowerCase() + "%"));
            }

            // ➕ Target Filters for the New Logistics Metrics Columns
            if (lrNumber != null && !lrNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lrNumber")), "%" + lrNumber.trim().toLowerCase() + "%"));
            }
            if (transporterName != null && !transporterName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("transporterName")), "%" + transporterName.trim().toLowerCase() + "%"));
            }
            if (vehicleNumber != null && !vehicleNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleNumber")), "%" + vehicleNumber.trim().toLowerCase() + "%"));
            }

            // Safe Parsing for the New LocalDate Column
            if (billDate != null && !billDate.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("billDate"), LocalDate.parse(billDate.trim())));
                } catch (DateTimeParseException e) {
                    predicates.add(criteriaBuilder.disjunction()); // Invalidate gracefully on broken user date inputs
                }
            }

            // Safe BigDecimal parsing for numeric column inputs
            if (grandTotal != null && !grandTotal.trim().isEmpty()) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("grandTotal"), new BigDecimal(grandTotal.trim())));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.disjunction());
                }
            }

            // 2. Global Text Input Cross-Cutting Search Override (OR logic block for single-field search bars)
            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
                String cleanSearch = "%" + globalSearch.trim().toLowerCase() + "%";

                Join<SalesBill, Customer> customerJoin = root.join("customer");
                Join<SalesBill, TypeOfBill> typeJoin = root.join("typeOfBill");

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), cleanSearch),
                        // ➕ Extended search to automatically scan your new columns
                        criteriaBuilder.like(criteriaBuilder.lower(typeJoin.get("name")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lrNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transporterName")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("vehicleNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ewayBillNumber")), cleanSearch),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("eInvoiceNumber")), cleanSearch)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


//public class SalesBillSpecification {
//
//    public static Specification<SalesBill> getDynamicSearchCriteria(
//            String globalSearch, String id, String billNumber, String financialYear, String customerName, String grandTotal) {
//
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // 1. Target Column Filters (AND logic block)
//            if (id != null && !id.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.equal(root.get("id"), Long.parseLong(id.trim())));
//            }
//            if (billNumber != null && !billNumber.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), "%" + billNumber.toLowerCase() + "%"));
//            }
//            if (financialYear != null && !financialYear.trim().isEmpty()) {
//                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), "%" + financialYear.toLowerCase() + "%"));
//            }
//
//            // CRITICAL JOIN: Dynamic SQL Join to filter by customer attributes
//            if (customerName != null && !customerName.trim().isEmpty()) {
//                Join<SalesBill, Customer> customerJoin = root.join("customer");
//                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + customerName.toLowerCase() + "%"));
//            }
//
//            // Safe BigDecimal parsing for numeric column inputs
//            if (grandTotal != null && !grandTotal.trim().isEmpty()) {
//                try {
//                    predicates.add(criteriaBuilder.equal(root.get("grandTotal"), new BigDecimal(grandTotal.trim())));
//                } catch (NumberFormatException e) {
//                    predicates.add(criteriaBuilder.disjunction()); // Fail gracefully if non-numbers are entered
//                }
//            }
//
//            // 2. Global Text Input Cross-Cutting Search Override (OR logic block)
//            if (globalSearch != null && !globalSearch.trim().isEmpty()) {
//                String cleanSearch = "%" + globalSearch.toLowerCase() + "%";
//                Join<SalesBill, Customer> customerJoin = root.join("customer");
//
//                predicates.add(criteriaBuilder.or(
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("billNumber")), cleanSearch),
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("financialYear")), cleanSearch),
//                        criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), cleanSearch)
//                ));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//}

