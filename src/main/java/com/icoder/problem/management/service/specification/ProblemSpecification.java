package com.icoder.problem.management.service.specification;

import com.icoder.problem.management.entity.Problem;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class ProblemSpecification implements Specification<Problem> {
    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Problem> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(
                    root.get(criteria.getKey()).as(String.class),
                    criteria.getValue().toString()
            );
        } else if (criteria.getOperation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(
                    root.get(criteria.getKey()).as(String.class),
                    criteria.getValue().toString()
            );
        } else if (criteria.getOperation().equalsIgnoreCase(":")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                        root.get(criteria.getKey()).as(String.class),
                        "%" + criteria.getValue() + "%"
                );
            } else {
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }

        return null;
    }
}
