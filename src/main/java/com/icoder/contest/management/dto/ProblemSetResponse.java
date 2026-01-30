package com.icoder.contest.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProblemSetResponse {
    private Long id;
    private String problemAlias;
    private int solvedCount;
    private int attemptedCount;
    private String title;
    private String origin; // maybe null
}
