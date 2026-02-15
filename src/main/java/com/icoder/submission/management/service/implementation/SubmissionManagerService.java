package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.provider.OnlineJudgeSubmissionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionManagerService {
    private final List<OnlineJudgeSubmissionProvider> providers;
    private final SubmissionPersistenceService submissionPersistenceService;

    public void initAndProcess(Long submissionId) {
        Submission submission = submissionPersistenceService.prepareSubmission(submissionId);

        BotAccount account = submissionPersistenceService.reserveAccount(submission.getOnlineJudge());

        try {
            log.info("Executing submission {} using account {}", submission.getId(), account.getUsername());

            OnlineJudgeSubmissionProvider provider = getProvider(submission.getOnlineJudge());
            SubmissionResult result = provider.submit(submission, account);

            submissionPersistenceService.finalizeSubmission(submissionId, result, account);

        } catch (Exception e) {
            log.error("Error during execution for submission {}: {}", submissionId, e.getMessage());
            submissionPersistenceService.handleProcessFailure(submissionId, account);
            throw e;
        }
    }

    private OnlineJudgeSubmissionProvider getProvider(OJudgeType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No provider found for judge: " + type));
    }
}
