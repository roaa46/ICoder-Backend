package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.submission.management.dto.OpenSubmissionResponse;
import com.icoder.submission.management.dto.OpenSubmissionResponse;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionPageResponse;
import com.icoder.submission.management.dto.SubmissionResponse;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.mapper.SubmissionMapper;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionMapper submissionMapper;
    private final SecurityUtils securityUtils;
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId) {
        Submission submission = submissionRepository.findByIdWithProblemAndUser(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isOwner = submission.getUser().getId().equals(currentUser.getId());
        boolean isOpen = submission.isOpened();

        if (!isOwner && !isOpen) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this submission");
        }

        if (isOwner) {
            OpenSubmissionResponse response = submissionMapper.toOpenSubmissionResponse(submission);
            response.setSolution(submission.getSubmissionCode());
            return response;
        }

        return submissionMapper.toSubmissionResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionPageResponse> getAllSubmissions(Pageable pageable) {
        Page<Submission> submissions = submissionRepository.findAll(pageable);
        return submissions.map(submission -> submissionMapper.toSubmissionPageResponse(
                submission,
                submission.getProblem().getProblemCode(),
                submission.getUser().getHandle()
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
    public boolean updateSubmissionOpen(Long submissionId, Authentication authentication) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Long currentUserId = securityUtils.getCurrentUserId();User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!submission.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own submissions");
        }

        submission.setOpened(!submission.isOpened());
        submissionRepository.save(submission);
        return submission.isOpened();
    }

    @Override
    public Integer getSolvedCount(String problemCode, OJudgeType onlineJudgeType) {
        return submissionRepository.getSolvedCount(problemCode, onlineJudgeType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionPageResponse> filterSubmissions(String userHandle, String oj, String problemCode, String language, Pageable pageable) {
        Page<Submission> submissions = submissionRepository.filterSubmissions(userHandle, oj, problemCode, language, pageable);
        return submissions.map(submission -> submissionMapper.toSubmissionPageResponse(
                submission,
                submission.getProblem().getProblemCode(),
                submission.getUser().getHandle()
        ));
    }

    @Override
    @Transactional
    public SubmissionResponse submit(SubmissionCreateRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")); log.info("Received submission request for problem: {} by user: {}",
                request.getProblemCode(), currentUser.getHandle());

        Problem problem = problemRepository.findByProblemCodeAndOnlineJudge(
                        request.getProblemCode(), request.getOnlineJudge())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        Submission submission = Submission.builder()
                .submissionCode(request.getCode())
                .language(request.getLanguage())
                .onlineJudge(problem.getOnlineJudge())
                .problem(problem)
                .user(currentUser)
                .status(SubmissionStatus.CREATED)
                .verdict(SubmissionVerdict.PENDING)
                .opened(false)
                .build();

        submission = submissionRepository.save(submission);
        log.info("Submission created with ID: {}", submission.getId());

        return submissionMapper.toSubmissionResponse(submission);
    }
}

