package com.icoder.problem.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.problem.management.enums.OJudgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProblemStatementResponse {
    private String problemCode;
    private String problemLink;
    private OJudgeType oJudgeSource;
    private String contestTitle;
    private String contestLink;
    private String problemTitle;
    private long solvedCount;
    private long attemptedCount;
    private List<SectionScrapeDTO> sections;
    private List<PropertyScrapeDTO> properties;
}
