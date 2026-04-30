package com.icoder.submission.management.service.implementation;

import com.icoder.contest.management.entity.Contest;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.specification.SpecBuilder;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.submission.management.dto.*;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.mapper.SubmissionMapper;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionProcessor submissionProcessor;
    private final SubmissionMapper submissionMapper;
    private final SecurityUtils securityUtils;
    private final SubmissionUtils submissionUtils;
    private final SubmissionPersistenceService submissionPersistenceService;

    @Cacheable(value = "languages", key = "#onlineJudge")
    public List<LanguageOptionResponse> getLanguages(String onlineJudge) {
        OJudgeType oJudgeType = OJudgeType.fromString(onlineJudge);
        return switch (oJudgeType) {
            case CSES -> submissionUtils.getCsesLanguages();
            case AT_CODER -> submissionUtils.getAtCoderLanguages();
            default -> submissionUtils.getCodeforcesLanguages();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithProblemAndUser(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        User currentUser = securityUtils.getCurrentUser();
        boolean isOwner = submission.getUser().getId().equals(currentUser.getId());
        boolean isOpen = submission.isOpened();

        if (!isOwner && !isOpen) {
            return submissionMapper.toSubmissionResponse(submission);
        }

        return submissionMapper.toOpenSubmissionResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionPageResponse> getAllSubmissions(String userHandle, String oj, String problemCode, String language, Pageable pageable) {

        OJudgeType judgeType = (oj != null) ? OJudgeType.fromString(oj) : null;

        Specification<Submission> spec = new SpecBuilder<Submission>()
                .with("onlineJudge", ":", judgeType)
                .with("problem.problemCode", ":", problemCode)
                .with("user.handle", ":", userHandle)
                .with("language", ":", language)
                .build();
        if (spec == null) spec = Specification.where(null);

        Page<Submission> submissions = submissionRepository.findAll(spec, pageable);
        return submissions.map(submission -> submissionMapper.toSubmissionPageResponse(
                submission,
                submission.getProblem().getProblemCode(),
                submission.getUser().getHandle(),
                submission.getUser().getId()
        ));
    }

    @Override
    @Transactional
    public SubmissionResponse save(Submission submission) {
        Submission savedSubmission = submissionRepository.save(submission);
        return submissionMapper.toSubmissionResponse(savedSubmission);
    }

    @Override
    @Transactional
    public boolean updateSubmissionOpen(Long submissionId) {
        User currentUser = securityUtils.getCurrentUser();
        log.info("Received request to update submission {} opened status", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (!submission.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own submissions");
        }

        submission.setOpened(!submission.isOpened());
        submissionRepository.save(submission);
        return submission.isOpened();
    }

    @Override
    @Transactional
    public SubmissionCreateResponse submit(SubmissionCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        log.info("Received submission request for problem: {} by user: {}",
                request.getProblemCode(), currentUser.getHandle());

        Problem problem = problemRepository.findByProblemCodeAndOnlineJudge(
                        request.getProblemCode(), request.getOnlineJudge())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        Contest contest = submissionUtils.validateSubmissionWithinContest(request, currentUser, problem);

        Submission submission = Submission.builder()
                .submissionCode(request.getCode())
                .language(request.getLanguage())
                .onlineJudge(problem.getOnlineJudge())
                .problem(problem)
                .user(currentUser)
                .status(SubmissionStatus.CREATED)
                .verdict(SubmissionVerdict.PENDING)
                .opened(request.isOpened())
                .contest(contest)
                .build();

        submission = submissionRepository.save(submission);
        log.info("Submission created with ID: {}", submission.getId());

        submissionUtils.updateUserProblemRelation(currentUser, problem);

        final Long submissionId = submission.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transaction committed successfully. Starting processor for submission ID: {}", submissionId);
                submissionProcessor.process(submissionId, request);
            }
        });

        return submissionMapper.toDTO(submission);
    }

    @Override
    public SessionSubmissionResponse addSessionId(SessionSubmissionRequest request) {
        User user = securityUtils.getCurrentUser();
        Long id = submissionPersistenceService.saveUserSession(user, request);
        return SessionSubmissionResponse.builder()
                .id(id)
                .build();
    }

    @Override
    public SessionSubmissionResponse updateSession(SessionSubmissionRequest request) {
        User user = securityUtils.getCurrentUser();
        Long id = submissionPersistenceService.updateUserSession(user, request);
        return SessionSubmissionResponse.builder()
                .id(id)
                .build();
    }

    @Override
    public void deleteSession(Long id) {
        submissionPersistenceService.deleteUserSession(id);
    }

    @Override
    public SessionSubmissionResponse getSession(String judgeType) {
        User user = securityUtils.getCurrentUser();
        return submissionPersistenceService.getUserSession(user, OJudgeType.fromString(judgeType));
    }
}