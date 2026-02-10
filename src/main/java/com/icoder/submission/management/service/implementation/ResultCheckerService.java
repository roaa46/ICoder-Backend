package com.icoder.submission.management.service.implementation;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.provider.OnlineJudgeSubmissionProvider;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.utils.SubmissionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultCheckerService {

    private final SubmissionRepository submissionRepository;
    private final List<OnlineJudgeSubmissionProvider> providers;
    private final SubmissionUtils submissionUtils;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void checkPendingSubmissions() {
        Set<Submission> pendingSubmissions = submissionRepository
                .findAllByVerdictIn(List.of(SubmissionVerdict.IN_QUEUE, SubmissionVerdict.PENDING, SubmissionVerdict.RUNNING));

        if (pendingSubmissions.isEmpty()) return;

        log.info("Checking verdicts for {} pending submissions", pendingSubmissions.size());

        for (Submission submission : pendingSubmissions) {
            try {
                OnlineJudgeSubmissionProvider provider = getProvider(submission.getOnlineJudge());

                SubmissionResult result = provider.checkVerdict(submission.getRemoteRunId(), submission.getBotAccount(), submission);

                if (result.verdict() != submission.getVerdict()) {
                    submission.setVerdict(result.verdict());
                    if (submissionUtils.isFinalVerdict(result.verdict())) {
                        submission.setStatus(SubmissionStatus.COMPLETED);
                    }
                    submission.setUpdatedAt(java.time.Instant.now());
                    submission.setTimeUsage(result.timeUsage());
                    submission.setMemoryUsage(result.memoryUsage());


                    submissionRepository.saveAndFlush(submission);

                    log.info("SUCCESS: Submission {} updated to {}", submission.getId(), result.verdict());
                }
            } catch (Exception e) {
                log.error("Failed to check verdict for submission {}: {}", submission.getId(), e.getMessage());
            }
        }
    }

    private OnlineJudgeSubmissionProvider getProvider(OJudgeType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow();
    }
}
