package com.icoder.submission.management.service.interfaces;


import com.icoder.submission.management.dto.*;
import com.icoder.submission.management.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SubmissionService {
    List<LanguageOptionResponse> getLanguages(String OnlineJudge);

    SubmissionResponse getSubmissionById(Long submissionId);

    Page<SubmissionPageResponse> getAllSubmissions(String userHandle, String oj, String problemCode, String language, Pageable pageable);

    SubmissionResponse save(Submission submission);

    boolean updateSubmissionOpen(Long submissionId, Authentication authentication);

    Integer getSolvedCount(String problemCode, com.icoder.problem.management.enums.OJudgeType onlineJudgeType);

    SubmissionCreateResponse submit(SubmissionCreateRequest request);
}
