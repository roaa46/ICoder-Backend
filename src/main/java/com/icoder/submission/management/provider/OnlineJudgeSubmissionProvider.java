package com.icoder.submission.management.provider;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionContext;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.Submission;

public interface OnlineJudgeSubmissionProvider {
    SubmissionResult submit(Submission submission, SubmissionContext context);

    boolean supports(OJudgeType type);

    SubmissionResult checkVerdict(String remoteRunId, Submission submission);
}
