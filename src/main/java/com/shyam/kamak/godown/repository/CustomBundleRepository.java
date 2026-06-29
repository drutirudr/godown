package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.Bundle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;

public interface CustomBundleRepository {
    // 🚀 Defines our high-performance custom slice slice method signature cleanly
    Slice<Bundle> fetchSliceWithGraph(Specification<Bundle> spec, Pageable pageable);
}
