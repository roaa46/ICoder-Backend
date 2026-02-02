package com.icoder.core.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {
    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<Object> path;
        if (criteria.getKey().contains(".")) {
            String[] parts = criteria.getKey().split("\\.");
            path = root.join(parts[0]).get(parts[1]);
        } else {
            path = root.get(criteria.getKey());
        }

        switch (criteria.getOperation()) {
            case ":":
                if (path.getJavaType() == String.class) {
                    return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
                            "%" + criteria.getValue().toString().toLowerCase() + "%");
                } else {
                    return criteriaBuilder.equal(path, criteria.getValue());
                }
            case ">":
                return criteriaBuilder.greaterThanOrEqualTo(path.as(String.class), criteria.getValue().toString());
            case "<":
                return criteriaBuilder.lessThanOrEqualTo(path.as(String.class), criteria.getValue().toString());
            default:
                return null;
        }
    }
}
