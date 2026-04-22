package com.icoder.contest.management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LeaderboardRowResponse {
    private int rank;
    private Long userId;
    private String handle;
    private int totalScore;
    private int totalPenalty;
    private Map<String, ProblemResultDto> problemResults; // Key: Problem Alias (A, B, C...)
}
