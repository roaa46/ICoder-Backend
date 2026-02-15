package com.icoder.submission.management.service.interfaces;


import com.icoder.submission.management.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SubmissionService {
    List<LanguageOptionResponse> getCsesLanguages();

    List<LanguageOptionResponse> getCodeforcesLanguages();

    List<LanguageOptionResponse> getAtCoderLanguages();

    SubmissionResponse getSubmissionById(Long submissionId);

    Page<SubmissionPageResponse> getAllSubmissions(Pageable pageable);

    SubmissionResponse save(com.icoder.submission.management.entity.Submission submission);

    boolean updateSubmissionOpen(Long submissionId, Authentication authentication);

    Integer getSolvedCount(String problemCode, com.icoder.problem.management.enums.OJudgeType onlineJudgeType);

    Page<SubmissionPageResponse> filterSubmissions(String userHandle, String oj, String problemCode, String language, Pageable pageable);

    SubmissionCreateResponse submit(SubmissionCreateRequest request);

    SubmissionCreateResponse getSubmission(Long id);
}
