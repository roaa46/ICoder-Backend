package com.icoder.activity.management.controller;

import com.icoder.activity.management.dto.ActivityLogResponse;
import com.icoder.activity.management.service.interfaces.ActivityLogService;
import com.icoder.submission.management.enums.SubmissionVerdict;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
@Tag(name = "activity-log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "List activity logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ActivityLogResponse>> getMyActivityLogs(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) SubmissionVerdict verdict) {
        Pageable pageable = PageRequest.of(pageNo, size);
        return ResponseEntity.ok(activityLogService.getMyActivityLogs(pageable, verdict));
    }
}
