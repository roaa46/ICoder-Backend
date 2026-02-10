package com.icoder.problem.management.service.specification;

import com.icoder.problem.management.entity.Problem;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class ProblemSpecificationsBuilder {
    private final List<SearchCriteria> params = new ArrayList<>();

    public ProblemSpecificationsBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<Problem> build() {
        if (params.isEmpty()) return null;

        Specification<Problem> result = new ProblemSpecification(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(new ProblemSpecification(params.get(i)));
        }

        return result;
    }
}
