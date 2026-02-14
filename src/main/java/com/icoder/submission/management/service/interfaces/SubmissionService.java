package com.icoder.submission.management.service.interfaces;

import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionPageResponse;
import com.icoder.submission.management.dto.SubmissionResponse;
import com.icoder.user.management.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface SubmissionService {

    SubmissionResponse getSubmissionById(Long submissionId);

    Page<SubmissionPageResponse> getAllSubmissions(Pageable pageable);

    SubmissionResponse save(com.icoder.submission.management.entity.Submission submission);

    boolean updateSubmissionOpen(Long submissionId, Authentication authentication);

    Integer getSolvedCount(String problemCode, com.icoder.problem.management.enums.OJudgeType onlineJudgeType);

    Page<SubmissionPageResponse> filterSubmissions(String userHandle, String oj, String problemCode, String language, Pageable pageable);

    SubmissionResponse submit(SubmissionCreateRequest request);

}

