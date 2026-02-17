package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SessionSubmissionRequest;
import com.icoder.submission.management.dto.SubmissionContext;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
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
    private final SubmissionPersistenceService submissionPersistenceService;
    private final List<OnlineJudgeSubmissionProvider> providers;

    public void initAndProcess(Long submissionId, SubmissionCreateRequest request) {
        Submission submission = submissionPersistenceService.prepareSubmission(submissionId);

        if (request instanceof SessionSubmissionRequest sessionReq) {
            submissionPersistenceService.saveOrUpdateUserSession(
                    submission.getUser(),
                    request.getOnlineJudge(),
                    sessionReq.getSessionId()
            );
        }

        SubmissionContext context = prepareContext(request);

        try {
            log.info("Preparing submission {} for execution", submissionId);

            OnlineJudgeSubmissionProvider provider = getProvider(submission.getOnlineJudge());
            SubmissionResult result = provider.submit(submission, context);

            submissionPersistenceService.finalizeSubmission(submissionId, result, context.account());

        } catch (Exception e) {
            log.error("Error during execution for submission {}: {}", submissionId, e.getMessage());
            submissionPersistenceService.handleProcessFailure(submissionId, context.account());
            throw e;
        }
    }

    private SubmissionContext prepareContext(SubmissionCreateRequest request) {
        if (request instanceof SessionSubmissionRequest sessionReq) {
            return new SubmissionContext(null, sessionReq.getSessionId());
        }
        BotAccount account = submissionPersistenceService.reserveAccount(request.getOnlineJudge());
        return new SubmissionContext(account, null);
    }

    private OnlineJudgeSubmissionProvider getProvider(OJudgeType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No provider found for judge: " + type));
    }
}
