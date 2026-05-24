package com.icoder.summary.management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserStatsDto {
    private long totalSubmissions;
    private double overallAcRate;
    private double recentAcRate;
    private int tleCount;
    private int rteCount;
    private int mleCount;
    private int compilationErrorCount;
    private List<String> strengths;
    private List<String> weaknesses;
    private String mostUsedLanguage;
}
