package com.icoder.submission.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.problem.management.enums.OJudgeType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubmissionCreateRequest {
    private String code;
    private String language;
    private OJudgeType onlineJudge;
    private Long problemId;
}
