package com.icoder.contest.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProblemResultDto {
    private boolean solved;
    private long solvedTime;
    private int wrongAttempts;
    private boolean firstAccepted;
}