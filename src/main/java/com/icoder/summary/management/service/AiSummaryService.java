package com.icoder.summary.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.summary.management.dto.UserStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSummaryService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateSummary(UserStatsDto stats) {
        try {
            String statsJson = objectMapper.writeValueAsString(stats);
            String prompt    = buildPrompt(statsJson);

            Map<String, Object> requestBody = Map.of(
                    "model", "llama3-8b-8192",
                    "messages", List.of(
                            Map.of("role", "system",
                                    "content", "You are an elite Competitive Programming Coach."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 1000
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Extract the text from Groq's response
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI summary: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String statsJson) {
        return """
            You are an elite Competitive Programming Coach analyzing a student's \
            problem-solving history on ICoder (aggregating Codeforces, CSES, AtCoder).

            Here are the user's statistics calculated by our backend:
            """ + statsJson + """

            Based on these statistics, generate a personalized, encouraging performance summary.
            Note:
            - High "tleCount" means the user struggles with time complexity / algorithm optimization.
            - High "rteCount" means the user struggles with edge cases or array out-of-bounds.
            - High "mleCount" means the user struggles with memory-efficient data structures.
            - High "compilationErrorCount" means the user should review syntax carefully.
            - If "recentAcRate" > "overallAcRate", highlight that they are actively improving!

            You MUST structure your response EXACTLY with these markdown headers. \
            Do not add any text outside these headers:

            ### Executive Summary
            [2-3 encouraging sentences. Mention improvement if recentAcRate > overallAcRate.]

            ### Strengths
            * **[Topic 1]:** [Why this is a strong foundation]
            * **[Topic 2]:** [Brief explanation]

            ### Weaknesses & Common Errors
            * **[Topic/Error 1]:** [Why they struggle and what it means]
            * **[Topic/Error 2]:** [Brief explanation]

            ### Recommended Study Resources
            * **[Resource 1]:** [Why they should study this — e.g. CP-Algorithms DP section]
            * **[Resource 2]:** [Second recommendation]

            ### Coach's Tip
            [One specific, actionable piece of advice based on their biggest weakness.]
            """;
    }
}