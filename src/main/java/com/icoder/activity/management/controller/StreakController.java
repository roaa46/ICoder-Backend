package com.icoder.activity.management.controller;

import com.icoder.activity.management.dto.StreakResponse;
import com.icoder.activity.management.service.interfaces.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-streak")
@RequiredArgsConstructor
@Tag(name = "activity-streak")
public class StreakController {

    private final StreakService streakService;

    @GetMapping()
    @Operation(summary = "Get User Streak")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreakResponse> getUserStreak(
            @RequestParam(required = false, defaultValue = "UTC") String timezone) {
        return ResponseEntity.ok(streakService.getUserStreak(timezone));
    }
}
