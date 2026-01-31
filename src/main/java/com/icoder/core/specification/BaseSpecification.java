package com.icoder.core.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {
    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (criteria.getKey().contains("."))
            return buildJoinPredicate(root, criteriaBuilder);

        switch (criteria.getOperation()) {
            case ">":
                return criteriaBuilder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
            case "<":
                return criteriaBuilder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
            case ":":
                if (root.get(criteria.getKey()).getJavaType() == String.class) {
                    return criteriaBuilder.like(criteriaBuilder.lower(root.get(criteria.getKey())), "%" + criteria.getValue().toString().toLowerCase() + "%");
                } else {
                    return criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
                }
            default:
                return null;
        }
    }

    private Predicate buildJoinPredicate(Root<T> root, CriteriaBuilder builder) {
        String[] parts = criteria.getKey().split("\\.");
        return builder.like(
                builder.lower(root.join(parts[0]).get(parts[1])),
                "%" + criteria.getValue().toString().toLowerCase() + "%"
        );
    }
}
