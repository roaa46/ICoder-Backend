package com.icoder.core.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {
    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<Object> path;
        if (criteria.getKey().contains(".")) {
            String[] parts = criteria.getKey().split("\\.");
            path = getOrCreateJoin(root, parts[0]).get(parts[1]);
        } else {
            path = root.get(criteria.getKey());
        }

        return switch (criteria.getOperation()) {
            case ":" -> path.getJavaType() == String.class
                    ? cb.like(cb.lower(path.as(String.class)),
                    "%" + criteria.getValue().toString().toLowerCase() + "%")
                    : cb.equal(path, criteria.getValue());
            case ">" -> cb.greaterThanOrEqualTo(path.as(String.class), criteria.getValue().toString());
            case "<" -> cb.lessThanOrEqualTo(path.as(String.class), criteria.getValue().toString());
            default -> null;
        };
    }

    private Join<Object, Object> getOrCreateJoin(Root<T> root, String attribute) {
        return root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(attribute))
                .map(j -> (Join<Object, Object>) j)
                .findFirst()
                .orElseGet(() -> root.join(attribute, JoinType.LEFT));
    }
}
