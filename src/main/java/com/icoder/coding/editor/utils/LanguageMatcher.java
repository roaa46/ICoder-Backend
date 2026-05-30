package com.icoder.coding.editor.utils;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LanguageMatcher {


    public static Map<Integer, String> JUDGE0_TO_MONACO_MAP = Map.ofEntries(
            // Python
            Map.entry(70, "python"), Map.entry(71, "python"), Map.entry(92, "python"),
            Map.entry(100, "python"), Map.entry(109, "python"), Map.entry(113, "python"),

            // C
            Map.entry(48, "c"), Map.entry(49, "c"), Map.entry(50, "c"),
            Map.entry(75, "c"), Map.entry(103, "c"), Map.entry(104, "c"), Map.entry(110, "c"),

            // C++
            Map.entry(52, "cpp"), Map.entry(53, "cpp"), Map.entry(54, "cpp"),
            Map.entry(76, "cpp"), Map.entry(105, "cpp"),

            // Java & JavaFX
            Map.entry(62, "java"), Map.entry(91, "java"), Map.entry(96, "java"),

            // JavaScript
            Map.entry(63, "javascript"), Map.entry(93, "javascript"),
            Map.entry(97, "javascript"), Map.entry(102, "javascript"),

            // TypeScript
            Map.entry(74, "typescript"), Map.entry(94, "typescript"), Map.entry(101, "typescript"),

            // Go
            Map.entry(60, "go"), Map.entry(95, "go"), Map.entry(106, "go"), Map.entry(107, "go"),

            // Rust
            Map.entry(73, "rust"), Map.entry(108, "rust"),

            // PHP
            Map.entry(68, "php"), Map.entry(98, "php"),

            // C#
            Map.entry(51, "csharp")
    );

    public static String resolveMonacoName(String judge0Name) {
        if (judge0Name == null) return "plaintext";
        String lowerName = judge0Name.toLowerCase().replace(" ", "");

        if (lowerName.contains("c++") || lowerName.contains("cpp")) return "cpp";
        if (lowerName.contains("c#") || lowerName.contains("csharp")) return "csharp";
        if (lowerName.contains("python")) return "python";
        if (lowerName.contains("java")) return "java";
        if (lowerName.contains("javascript") || lowerName.contains("node")) return "javascript";
        if (lowerName.contains("typescript")) return "typescript";
        if (lowerName.startsWith("c(") || lowerName.equals("c")) return "c";
        if (lowerName.contains("php")) return "php";
        if (lowerName.contains("go")) return "go";
        if (lowerName.contains("rust")) return "rust";

        return "plaintext";
    }
}
