package com.icoder.contest.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.problem.management.enums.OJudgeType;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String problemAlias;
    private int solvedCount;
    private int attemptedCount;
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String origin;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long problemId;
    private boolean solved;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private OJudgeType judgeType;
    private String problemCode;
    private int problemNumber;
}
