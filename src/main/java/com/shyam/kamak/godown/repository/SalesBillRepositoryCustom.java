package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.SalesBill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

public interface SalesBillRepositoryCustom {
    // 🚀 Defines your high-speed custom slice method signature clearly
    Slice<SalesBill> fetchSliceWithSpecification(Specification<SalesBill> spec, Pageable pageable);
}
