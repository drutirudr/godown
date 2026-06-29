package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Bundle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import java.util.List;

public class CustomBundleRepositoryImpl implements CustomBundleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Slice<Bundle> fetchSliceWithGraph(Specification<Bundle> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Query 1: Fetch rows matching specification (plus 1 extra row to see if there is a next page)
        CriteriaQuery<Bundle> query = cb.createQuery(Bundle.class);
        Root<Bundle> root = query.from(Bundle.class);

        // 🚀 CRITICAL: Force Left Join Fetch to eliminate the N+1 lag completely
        root.fetch("items", JoinType.LEFT).fetch("fabric", JoinType.LEFT);

        query.select(root);

        // Apply your tab-filtering dynamic specifications safely
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Apply your sorting order (id DESC)
        if (pageable.getSort().isSorted()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));
        }

        TypedQuery<Bundle> typedQuery = entityManager.createQuery(query);

        // Fetch size + 1 rows to determine if a next page exists without using a COUNT(*) query
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize() + 1);

        List<Bundle> resultList = typedQuery.getResultList();

        // Check if there are more records beyond this page slice
        boolean hasNext = resultList.size() > pageable.getPageSize();
        if (hasNext) {
            resultList.remove(pageable.getPageSize()); // Drop the extra indicator row
        }

        return new SliceImpl<>(resultList, pageable, hasNext);
    }
}
