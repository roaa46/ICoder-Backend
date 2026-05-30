package com.icoder.submission.management.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LanguageNormalizer {

    private static final List<Map.Entry<String, String>> RULES = List.of(
            Map.entry("c++", "CPP"),
            Map.entry("g++", "CPP"),
            Map.entry("gcc ", "CPP"),
            Map.entry("c23", "C"),
            Map.entry("java", "JAVA"),
            Map.entry("kotlin", "KOTLIN"),
            Map.entry("scala", "SCALA"),
            Map.entry("python", "PYTHON"),
            Map.entry("cpython", "PYTHON"),
            Map.entry("pypy", "PYTHON"),
            Map.entry("javascript", "JAVASCRIPT"),
            Map.entry("node", "JAVASCRIPT"),
            Map.entry("typescript", "TYPESCRIPT"),
            Map.entry("rust", "RUST"),
            Map.entry("go ", "GO"),
            Map.entry("go1", "GO"),
            Map.entry("php", "PHP"),
            Map.entry("ruby", "RUBY"),
            Map.entry("c#", "CSHARP"),
            Map.entry(".net", "CSHARP"),
            Map.entry("haskell", "HASKELL"),
            Map.entry("pascal", "PASCAL"),
            Map.entry("swift", "SWIFT"),
            Map.entry("perl", "PERL"),
            Map.entry("ocaml", "OCAML"),
            Map.entry("f#", "FSHARP"),
            Map.entry("d dmd", "D"),
            Map.entry("scala", "SCALA")
    );

    public static Optional<String> normalize(String language) {
        if (language == null || language.isBlank()) return Optional.empty();
        String lower = language.trim().toLowerCase();
        return RULES.stream()
                .filter(e -> lower.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
