package com.icoder.summary.management.controller;

import com.icoder.summary.management.dto.UserStatsDto;
import com.icoder.summary.management.service.AiSummaryService;
import com.icoder.summary.management.service.SummaryAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/summary")
@Tag(name = "Summary", description = "AI-powered performance summary")
@Slf4j
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryAggregationService aggregationService;
    private final AiSummaryService aiSummaryService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get AI performance summary",
            description = "Aggregates user submissions and returns an AI-generated coaching summary"
    )
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Long userId) {
        log.info("Requesting summary for user: {}", userId);

        UserStatsDto stats = aggregationService.aggregateUserStats(userId);
        String aiSummary = aiSummaryService.generateSummary(stats);

        return ResponseEntity.ok(Map.of(
                "stats", stats,
                "summary", aiSummary
        ));
    }

    @GetMapping("/{userId}/stats-only")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get raw stats only (no AI)",
            description = "Returns the aggregated stats without calling the AI — useful for testing"
    )
    public ResponseEntity<UserStatsDto> getStatsOnly(@PathVariable Long userId) {
        return ResponseEntity.ok(aggregationService.aggregateUserStats(userId));
    }
}