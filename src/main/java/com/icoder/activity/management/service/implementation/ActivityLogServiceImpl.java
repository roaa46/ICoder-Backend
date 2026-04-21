package com.icoder.activity.management.service.implementation;

import com.icoder.activity.management.dto.ActivityLogResponse;
import com.icoder.activity.management.entity.ActivityLog;
import com.icoder.activity.management.enums.ActivityType;
import com.icoder.activity.management.repository.ActivityLogRepository;
import com.icoder.activity.management.service.interfaces.ActivityLogService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getMyActivityLogs(Pageable pageable, SubmissionVerdict verdict) {
        Long userId = securityUtils.getCurrentUserId();
        return activityLogRepository.findByUserWithVerdict(userId, verdict, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void logSubmission(User user, Long submissionId, SubmissionVerdict verdict) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .activityType(ActivityType.SUBMISSION)
                .entityType("SUBMISSION")
                .entityId(submissionId)
                .verdict(verdict)
                .build();
        activityLogRepository.save(log);
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .userHandle(log.getUser().getHandle())
                .activityType(log.getActivityType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .verdict(log.getVerdict())
                .createdAt(log.getCreatedAt())
                .build();
    }
}