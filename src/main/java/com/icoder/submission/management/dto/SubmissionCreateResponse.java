package com.icoder.submission.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SubmissionCreateResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String remoteRunId;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private SubmissionStatus status;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private SubmissionVerdict verdict;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer timeUsage;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer memoryUsage;
    private String problemCode;
    private Instant submittedAt;
}
