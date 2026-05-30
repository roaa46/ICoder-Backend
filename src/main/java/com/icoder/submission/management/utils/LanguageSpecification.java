package com.icoder.submission.management.utils;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class LanguageSpecification<T> implements Specification<T> {
    private final String normalizedFamily;

    public LanguageSpecification(String normalizedFamily) {
        this.normalizedFamily = normalizedFamily;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Expression<String> lang = cb.lower(root.get("language"));
        return switch (normalizedFamily) {
            case "CPP" -> cb.or(
                    cb.like(lang, "%c++%"),
                    cb.like(lang, "%g++%")
            );
            case "C" -> cb.and(
                    cb.or(
                            cb.like(lang, "%gcc c%"),
                            cb.like(lang, "c23%"),
                            cb.like(lang, "% c23%")
                    ),
                    cb.not(cb.like(lang, "%c++%")),
                    cb.not(cb.like(lang, "%g++%"))
            );
            case "JAVA" -> cb.and(
                    cb.like(lang, "%java%"),
                    cb.not(cb.like(lang, "%javascript%"))
            );
            case "PYTHON" -> cb.or(
                    cb.like(lang, "%python%"),
                    cb.like(lang, "%cpython%"),
                    cb.like(lang, "%pypy%")
            );
            case "JAVASCRIPT" -> cb.or(
                    cb.like(lang, "%javascript%"),
                    cb.like(lang, "%node%")
            );
            case "CSHARP" -> cb.or(
                    cb.like(lang, "%c#%"),
                    cb.like(lang, "%.net%")
            );
            case "GO" -> cb.or(
                    cb.like(lang, "%go %"),
                    cb.like(lang, "%go1%")
            );
            default -> cb.like(lang, "%" + normalizedFamily.toLowerCase() + "%");
        };
    }
}
