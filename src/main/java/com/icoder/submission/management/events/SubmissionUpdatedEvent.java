package com.icoder.submission.management.events;

import com.icoder.submission.management.enums.SubmissionVerdict;

public record SubmissionUpdatedEvent(Long submissionId, Long userId, SubmissionVerdict verdict) {
}