package com.icoder.problem.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.problem.management.enums.OJudgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProblemResponse {
    private String problemCode;
    private String problemLink;
    private OJudgeType onlineJudge;
    private String contestTitle;
    private String contestLink;
    private String problemTitle;
    private long solvedCount;
    private Instant fetchedAt;
}
