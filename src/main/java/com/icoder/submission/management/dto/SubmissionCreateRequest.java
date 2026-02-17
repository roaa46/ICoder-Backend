package com.icoder.submission.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.core.utils.UppercaseEnumDeserializer;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.enums.SubmissionMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubmissionCreateRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    @NotBlank
    private String problemCode;
    @NotBlank
    private String code;
    @NotBlank
    private String language;
    @NotNull
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private OJudgeType onlineJudge;

    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private SubmissionMethod submissionMethod;

    private Long contestId; // optional
}
