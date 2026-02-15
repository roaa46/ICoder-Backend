package com.icoder.submission.management.service.implementation;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.events.SubmissionUpdatedEvent;
import com.icoder.submission.management.provider.OnlineJudgeSubmissionProvider;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.utils.SubmissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultCheckerService {

    private final SubmissionRepository submissionRepository;
    private final List<OnlineJudgeSubmissionProvider> providers;
    private final SubmissionUtils submissionUtils;
    private final SubmissionTaskPublisher submissionTaskPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void checkSingleSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElse(null);

        if (submission == null) {
            log.warn("Submission {} not found, skipping check", submissionId);
            return;
        }

        if (submissionUtils.isFinalVerdict(submission.getVerdict())) {
            log.info("Submission {} already has a final verdict: {}", submissionId, submission.getVerdict());
            return;
        }

        try {
            log.info("Checking verdict for submission {} on {}", submissionId, submission.getOnlineJudge());

            OnlineJudgeSubmissionProvider provider = getProvider(submission.getOnlineJudge());
            SubmissionResult result = provider.checkVerdict(submission.getRemoteRunId(), submission.getBotAccount(), submission);

            if (result.verdict() != submission.getVerdict()) {
                submission.setVerdict(result.verdict());

                if (submissionUtils.isFinalVerdict(result.verdict())) {
                    submission.setStatus(SubmissionStatus.COMPLETED);
                }

                submission.setUpdatedAt(Instant.now());
                submission.setTimeUsage(result.timeUsage());
                submission.setMemoryUsage(result.memoryUsage());

                submissionRepository.saveAndFlush(submission);

                eventPublisher.publishEvent(new SubmissionUpdatedEvent(submission.getId(), submission.getVerdict()));

                log.info("SUCCESS: Submission {} updated to {}", submissionId, result.verdict());

                if (result.verdict() == SubmissionVerdict.ACCEPTED) {
                    submissionUtils.updateRelationAsSolved(submission);
                }
            }

            if (!submissionUtils.isFinalVerdict(submission.getVerdict())) {
                log.info("Submission {} still pending, rescheduling check...", submissionId);
                submissionTaskPublisher.publishCheckTask(submissionId);
            }

        } catch (Exception e) {
            log.error("Failed to check verdict for submission {}: {}", submissionId, e.getMessage());
            submissionTaskPublisher.publishCheckTask(submissionId);
        }
    }

    private OnlineJudgeSubmissionProvider getProvider(OJudgeType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No provider found for judge: " + type));
    }
}