package com.icoder.submission.management.dto;

import com.icoder.submission.management.enums.SubmissionVerdict;

public record SubmissionResult(String remoteRunId,
                               SubmissionVerdict verdict,
                               Integer timeUsage,
                               Integer memoryUsage,
                               String errorMessage) {
}
