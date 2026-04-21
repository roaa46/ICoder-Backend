package com.icoder.activity.management.service.interfaces;

import com.icoder.activity.management.dto.ActivityLogResponse;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.user.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ActivityLogService {

    Page<ActivityLogResponse> getMyActivityLogs(Pageable pageable, SubmissionVerdict verdict);


    @Transactional
    void logSubmission(User user, Long submissionId, SubmissionVerdict verdict);
}
