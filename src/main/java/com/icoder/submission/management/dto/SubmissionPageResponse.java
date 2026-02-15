package com.icoder.submission.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.enums.SubmissionVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionPageResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String userHandle;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private OJudgeType onlineJudge;

    private String problemCode;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private SubmissionVerdict verdict;

    private String language;

    private String timeUsage;

    private String memoryUsage;

    private Instant submittedAt;

    private Boolean isOpen;

    private String remoteRunId;
}

