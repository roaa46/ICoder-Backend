package com.icoder.submission.management.service.implementation;

import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    @Override
    public SubmissionCreateResponse createSubmission(SubmissionCreateRequest request, User user) {
        return null;
    }
}