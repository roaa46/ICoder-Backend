package com.icoder.submission.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String remoteRunId;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private OJudgeType onlineJudge;

    private String language;

    private Instant submittedAt;

    private String memoryUsage;

    private String timeUsage;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private SubmissionVerdict verdict;

    private Boolean isOpen;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private SubmissionStatus status;

    private String problemCode;

    private String userHandle;
}

