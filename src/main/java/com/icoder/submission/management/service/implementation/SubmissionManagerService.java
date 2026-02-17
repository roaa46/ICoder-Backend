package com.icoder.submission.management.service.implementation;

import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.provider.OnlineJudgeSubmissionProvider;
import com.icoder.submission.management.utils.SubmissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionManagerService {
    private final SubmissionPersistenceService submissionPersistenceService;
    private final SubmissionUtils submissionUtils;

    public void initAndProcess(Long submissionId) {
        Submission submission = submissionPersistenceService.prepareSubmission(submissionId);

        BotAccount account = submissionPersistenceService.reserveAccount(submission.getOnlineJudge());

        try {
            log.info("Executing submission {} using account {}", submission.getId(), account.getUsername());

            OnlineJudgeSubmissionProvider provider = submissionUtils.getProvider(submission.getOnlineJudge());
            SubmissionResult result = provider.submit(submission, account);

            submissionPersistenceService.finalizeSubmission(submissionId, result, account);

        } catch (Exception e) {
            log.error("Error during execution for submission {}: {}", submissionId, e.getMessage());
            submissionPersistenceService.handleProcessFailure(submissionId, account);
            throw e;
        }
    }
}
