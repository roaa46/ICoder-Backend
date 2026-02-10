package com.icoder.submission.management.provider;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;

public interface OnlineJudgeSubmissionProvider {
    SubmissionResult submit(Submission submission, BotAccount account);

    boolean supports(OJudgeType type);

    SubmissionResult checkVerdict(String remoteRunId, BotAccount account, Submission submission);
}
