package com.icoder.contest.management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
public class LeaderboardRowResponse {
    Map<String, ProblemResultDto> problemResults = new LinkedHashMap<>(); // Key: Problem Alias (A, B, C...)
    private int rank;
    private Long userId;
    private String handle;
    private int totalScore;
    private int totalPenalty;
}
