package com.icoder.submission.management.service.interfaces;


import com.icoder.submission.management.dto.LanguageOptionResponse;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionCreateResponse;

import java.util.List;

public interface SubmissionService {
    List<LanguageOptionResponse> getCsesLanguages();
    List<LanguageOptionResponse> getCodeforcesLanguages();
    List<LanguageOptionResponse> getAtCoderLanguages();

    SubmissionCreateResponse submit(SubmissionCreateRequest request);

    SubmissionCreateResponse getSubmission(Long id);
}
