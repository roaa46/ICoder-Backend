package com.icoder.contest.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProblemSetResponse {
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String problemAlias;
    private int solvedCount;
    private int attemptedCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title; // maybe null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String origin; // maybe null
}
