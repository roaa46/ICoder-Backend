package com.icoder.activity.management.service.implementation;

import com.icoder.activity.management.dto.ActivityGridResponse;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<ActivityGridResponse> getUserActivityGrid(Long userId, int year, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);

        ZonedDateTime startOfYear = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zoneId);
        ZonedDateTime endOfYear = ZonedDateTime.of(year, 12, 31, 23, 59, 59, 999999999, zoneId);

        Instant startDate = startOfYear.toInstant();
        Instant endDate = endOfYear.toInstant();

        List<ActivityLog> logs = activityLogRepository.findUserActivitiesInRange(userId, startDate, endDate);

        Map<LocalDate, List<ActivityLog>> groupedByDate = logs.stream()
                .collect(Collectors.groupingBy(log -> log.getCreatedAt().atZone(zoneId).toLocalDate()));

        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<ActivityLog> dailyLogs = entry.getValue();

                    long acceptedCount = dailyLogs.stream()
                            .filter(log -> log.getVerdict() == SubmissionVerdict.ACCEPTED)
                            .count();

                    long attemptedCount = dailyLogs.stream()
                            .filter(log -> log.getVerdict() != SubmissionVerdict.ACCEPTED)
                            .count();

                    return ActivityGridResponse.builder()
                            .date(date)
                            .acceptedCount(acceptedCount)
                            .attemptedCount(attemptedCount)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
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