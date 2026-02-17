package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SessionSubmissionRequest;
import com.icoder.submission.management.dto.SessionSubmissionResponse;
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
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionPersistenceService {
    private final SubmissionRepository submissionRepository;
    private final BotAccountRepository accountRepository;
    private final SubmissionUtils submissionUtils;
    private final SubmissionTaskPublisher submissionTaskPublisher;
    private final UserJudgeSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission prepareSubmission(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithProblem(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        submission.setStatus(SubmissionStatus.SUBMITTING);
        log.info("Preparing submission {} for execution", submissionId);
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
        log.info("Submission {} finalized with verdict {}", submissionId, result.verdict());
    }

    @Transactional
    public void handleProcessFailure(Long submissionId, BotAccount account) {
        submissionUtils.handleFailure(submissionId);
        if (account != null) {
            releaseAccount(account);
        }
        log.error("Submission {} failed due to an error in execution", submissionId);
    }

    @Transactional
    public BotAccount reserveAccount(OJudgeType type) {
        BotAccount account = accountRepository
                .findFirstByJudgeTypeAndActiveTrueAndInUseFalseOrderByLastUsedAtAsc(type)
                .orElseThrow(() -> new ResourceNotFoundException("No available bot accounts for " + type));

        account.setInUse(true);
        log.info("Reserved account {} for execution", account.getId());
        return accountRepository.save(account);
    }

    private void releaseAccount(BotAccount account) {
        account.setInUse(false);
        account.setLastUsedAt(Instant.now());
        accountRepository.save(account);
        log.info("Released account {}", account.getId());
    }

    @Transactional
    public Long saveUserSession(User user, SessionSubmissionRequest request) {
        UserJudgeSession session = UserJudgeSession.builder()
                .user(user)
                .judgeType(request.getOnlineJudge())
                .build();

        session.setSessionData(request.getSessionData());
        session.setLastUpdated(Instant.now());
        sessionRepository.save(session);
        log.info("Session created for user {} with session id {}", user.getId(), session.getId());
        return session.getId();
    }

    @Transactional
    public void deleteUserSession(Long sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Session deleted with id {}", sessionId);
    }

    @Transactional
    public Long updateUserSession(User user, SessionSubmissionRequest request) {
        UserJudgeSession session = sessionRepository
                .findByUserAndJudgeType(user, request.getOnlineJudge())
                .orElseThrow(() -> new ResourceNotFoundException("Session Not Found"));
        session.setSessionData(request.getSessionData());
        session.setLastUpdated(Instant.now());
        sessionRepository.save(session);

        log.info("Session updated for user {} with session id {}", user.getId(), session.getId());
        return session.getId();
    }

    @Transactional
    public SessionSubmissionResponse getUserSession(User user, OJudgeType judgeType) {
        UserJudgeSession session = sessionRepository
                .findByUserAndJudgeType(user, judgeType)
                .orElseThrow(() -> new ResourceNotFoundException("Session Not Found"));

        log.info("Session found for user {} with session id {}", user.getId(), session.getId());

        return SessionSubmissionResponse.builder()
                .id(session.getId())
                .build();
    }

    @Transactional
    public String getUserSessionData(Long userId, OJudgeType judgeType) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return sessionRepository.findByUserAndJudgeType(currentUser, judgeType).get().getSessionData();
    }
}
