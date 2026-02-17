package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.entity.UserJudgeSession;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.repository.UserJudgeSessionRepository;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SubmissionPersistenceService {
    private final SubmissionRepository submissionRepository;
    private final BotAccountRepository accountRepository;
    private final SubmissionUtils submissionUtils;
    private final SubmissionTaskPublisher submissionTaskPublisher;
    private final UserJudgeSessionRepository sessionRepository;

    @Transactional
    public Submission prepareSubmission(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithProblem(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        submission.setStatus(SubmissionStatus.SUBMITTING);
        return submissionRepository.save(submission);
    }

    @Transactional
    public void finalizeSubmission(Long submissionId, SubmissionResult result, BotAccount account) {
        Submission submission = submissionRepository.findById(submissionId).get();
        submission.setBotAccount(account);
        submission.setRemoteRunId(result.remoteRunId());
        submission.setVerdict(result.verdict());
        submission.setTimeUsage(result.timeUsage());
        submission.setMemoryUsage(result.memoryUsage());

        if (result.verdict() == SubmissionVerdict.FAILED) {
            submission.setStatus(SubmissionStatus.FAILED);
        } else if (!submissionUtils.isFinalVerdict(result.verdict())) {
            submissionTaskPublisher.publishCheckTask(submissionId);
        }

        submissionRepository.save(submission);

        if (submission.getVerdict() == SubmissionVerdict.ACCEPTED) {
            submissionUtils.updateRelationAsSolved(submission);
        }

        if (account != null)
            releaseAccount(account);
    }

    @Transactional
    public void handleProcessFailure(Long submissionId, BotAccount account) {
        submissionUtils.handleFailure(submissionId);
        if (account != null) {
            releaseAccount(account);
        }
    }

    @Transactional
    public BotAccount reserveAccount(OJudgeType type) {
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

    @Transactional
    public void saveOrUpdateUserSession(User user, OJudgeType judge, String sessionId) {
        UserJudgeSession session = sessionRepository
                .findByUserAndJudgeType(user, judge)
                .orElseGet(() -> UserJudgeSession.builder()
                        .user(user)
                        .judgeType(judge)
                        .build());

        session.setSessionData(sessionId);
        session.setLastUpdated(Instant.now());
        sessionRepository.save(session);
    }
}
