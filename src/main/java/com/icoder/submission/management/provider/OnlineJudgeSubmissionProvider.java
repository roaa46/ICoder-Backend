package com.icoder.submission.management.provider;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.OnlineJudgeAccount;
import com.icoder.submission.management.entity.Submission;

public interface OnlineJudgeSubmissionProvider {
    Submission submit(Submission submission, OnlineJudgeAccount account);

    boolean supports(OJudgeType type);
}
