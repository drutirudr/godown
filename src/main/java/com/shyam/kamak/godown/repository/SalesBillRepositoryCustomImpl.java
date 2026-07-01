package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.SalesBill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SalesBillRepositoryCustomImpl implements SalesBillRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Slice<SalesBill> fetchSliceWithSpecification(Specification<SalesBill> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SalesBill> query = cb.createQuery(SalesBill.class);
        Root<SalesBill> root = query.from(SalesBill.class);

        // 🚀 HIGH-SPEED GRAPH TUNER: Forces a single optimized join-fetch pass
        root.fetch("customer", JoinType.LEFT);
        root.fetch("typeOfBill", JoinType.LEFT);
        Fetch<Object, Object> itemsFetch = root.fetch("items", JoinType.LEFT);
        itemsFetch.fetch("bundle", JoinType.LEFT);

        // 🎯 STITCH PREDICATES: Compile your calculated date boundaries cleanly into the query!
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Apply standardized index sorting rules
        query.orderBy(cb.desc(root.get("id")));

        TypedQuery<SalesBill> typedQuery = entityManager.createQuery(query);

        // Pagination slicing offsets
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize() + 1); // Query 1 extra row to check for more pages

        List<SalesBill> resultList = typedQuery.getResultList();

        // Check if there are more records beyond the current page chunk
        boolean hasNext = resultList.size() > pageable.getPageSize();
        if (hasNext) {
            resultList.remove(pageable.getPageSize()); // Drop the extra safety indicator row
        }

        return new SliceImpl<>(resultList, pageable, hasNext);
    }
}
