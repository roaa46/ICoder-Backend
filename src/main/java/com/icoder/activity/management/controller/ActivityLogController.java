package com.icoder.activity.management.controller;

import com.icoder.activity.management.dto.ActivityGridResponse;
import com.icoder.activity.management.dto.ActivityLogResponse;
import com.icoder.activity.management.service.interfaces.ActivityLogService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.submission.management.enums.SubmissionVerdict;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-logs")
@Tag(name = "Activity Logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final SecurityUtils securityUtils;

    @GetMapping("/my")
    @Operation(summary = "Get My Activity Logs", description = "Retrieve a paginated list of activity logs for the current user")
    public ResponseEntity<Page<ActivityLogResponse>> getMyActivityLogs(
            Pageable pageable,
            @RequestParam(required = false) SubmissionVerdict verdict) {
        return ResponseEntity.ok(activityLogService.getMyActivityLogs(pageable, verdict));
    }

    @GetMapping("/grid")
    @Operation(summary = "Get Activity Grid", description = "Retrieve a list of activity grid data for the current user")
    public ResponseEntity<List<ActivityGridResponse>> getActivityGrid(
            @RequestParam int year,
            @RequestParam(required = false, defaultValue = "UTC") String timezone) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(activityLogService.getUserActivityGrid(userId, year, timezone));
    }
}
