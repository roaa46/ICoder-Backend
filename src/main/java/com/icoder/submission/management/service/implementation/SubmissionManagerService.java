package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.provider.OnlineJudgeSubmissionProvider;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.submission.management.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionManagerService {
    private final List<OnlineJudgeSubmissionProvider> providers;
    private final BotAccountRepository accountRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public void processSubmission(Submission submission) {
        OnlineJudgeSubmissionProvider provider = getProvider(submission.getOnlineJudge());

        BotAccount account = reserveAccount(submission.getOnlineJudge());

        try {
            log.info("Executing submission {} using account {}", submission.getId(), account.getUsername());

            submission.setBotAccount(account);

            SubmissionResult result = provider.submit(submission, account);

            submission.setRemoteRunId(result.remoteRunId());
            submission.setVerdict(result.verdict());

            if (result.verdict() != SubmissionVerdict.FAILED) {
                submission.setStatus(SubmissionStatus.COMPLETED);
            } else {
                submission.setStatus(SubmissionStatus.FAILED);
            }

            log.info("Submission {} updated with Remote ID: {} and Verdict: {}",
                    submission.getId(), result.remoteRunId(), result.verdict());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during provider execution for submission {}: {}", submission.getId(), e.getMessage());
            throw e;
        } finally {
            releaseAccount(account);
        }
    }

    @Transactional
    public void initAndProcess(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithProblem(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with ID: " + submissionId));

        submission.setStatus(SubmissionStatus.SUBMITTING);
        submissionRepository.save(submission);

        this.processSubmission(submission);
    }

    private OnlineJudgeSubmissionProvider getProvider(OJudgeType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No provider found for judge: " + type));
    }

    private BotAccount reserveAccount(OJudgeType type) {
        BotAccount account = accountRepository
                .findFirstByJudgeTypeAndActiveTrueAndInUseFalseOrderByLastUsedAtAsc(type)
                .orElseThrow(() -> new ResourceNotFoundException("No available bot accounts for " + type));

        account.setInUse(true);
        return accountRepository.save(account);
    }

    private void releaseAccount(BotAccount account) {
        account.setInUse(false);
        account.setLastUsedAt(Instant.now());
        accountRepository.save(account);
    }
}
