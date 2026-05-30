package com.icoder.core.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecBuilder<T> {
    private final List<SearchCriteria> params = new ArrayList<>();

    public SpecBuilder<T> with(String key, String operation, Object value) {
        if (value != null) {
            params.add(new SearchCriteria(key, operation, value));
        }
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) return null;

        Specification<T> result = new BaseSpecification<>(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(new BaseSpecification<>(params.get(i)));
        }

        return result;
    }
}
