package com.icoder.problem.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    private String onlineJudge;  // use .toString()..toLowerCase() while scrapping
    private String contestTitle;
    private String contestLink;
    private String problemTitle;
    private long solvedCount;
    private Instant fetchedAt;
}
