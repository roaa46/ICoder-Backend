package com.icoder.submission.management.service.interfaces;


import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.user.management.entity.User;

public interface SubmissionService {
    SubmissionCreateResponse createSubmission(SubmissionCreateRequest request, User user);
}
