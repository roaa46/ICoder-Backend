package com.icoder.submission.management.dto;

import com.icoder.submission.management.enums.SubmissionVerdict;

import java.time.Instant;

public interface SubmissionSummary {
    Long getId();

    Long getUserId();

    Long getProblemId();

    SubmissionVerdict getVerdict();

    Instant getCreatedAt();
}
