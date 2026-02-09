package com.icoder.submission.management.dto;

import com.icoder.submission.management.enums.SubmissionVerdict;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionCreateResponse {
    private String submissionId;
    private String remoteRunId;
    private SubmissionVerdict verdict;
    private Integer timeUsage;
    private Integer memoryUsage;
}
