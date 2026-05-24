package com.icoder.summary.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.summary.management.dto.UserStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private static final String PREFERRED_MODEL = "llama-3.3-70b-versatile";
    private final WebClient groqWebClient;
    private final ObjectMapper objectMapper;
    @Value("${groq.api.key}")
    private String groqApiKey;

    public String generateSummary(UserStatsDto stats) {
        String modelToUse = getBestAvailableModel();
        log.info("Generating summary using model: {}", modelToUse);
        try {
            String statsJson = objectMapper.writeValueAsString(stats);

            Map<String, Object> requestBody = Map.of(
                    "model", modelToUse,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are an elite Competitive Programming Coach."),
                            Map.of("role", "user", "content", buildPrompt(statsJson))
                    ),
                    "temperature", 0.7,
                    "max_tokens", 1000
            );

            return groqWebClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException))
                    .map(this::extractContent)
                    .block();

        } catch (Exception e) {
            log.error("Failed to generate AI summary: {}", e.getMessage());
            throw new RuntimeException("AI Summary Service currently unavailable.", e);
        }
    }

    private String getBestAvailableModel() {
        List<String> priorityList = List.of(
                "llama-3.3-70b-versatile",
                "llama-3.1-70b-versatile",
                "mixtral-8x7b-32768"
        );

        try {
            Map response = groqWebClient.get()
                    .uri("/models")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            List<String> availableIds = data.stream()
                    .map(m -> (String) m.get("id"))
                    .toList();

            return priorityList.stream()
                    .filter(availableIds::contains)
                    .findFirst()
                    .orElse(availableIds.get(0));

        } catch (Exception e) {
            log.error("Failed to fetch models, defaulting to: {}", PREFERRED_MODEL);
            return PREFERRED_MODEL;
        }
    }

    private String extractContent(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
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