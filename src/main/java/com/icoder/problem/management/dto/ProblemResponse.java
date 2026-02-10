package com.icoder.problem.management.dto;

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

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProblemResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long problemId;
    private String problemCode;
    private String problemLink;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private OJudgeType onlineJudge;  // use .toString()..toLowerCase() while scrapping
    private String contestTitle;
    @JsonInclude(JsonInclude.Include.NON_NULL) // contestLink is null in CSES
    private String contestLink;
    private String problemTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long solvedCount;
    private Instant fetchedAt;
    private boolean isSolved;
    private boolean isAttempted;
    private boolean isFavorite;
}
