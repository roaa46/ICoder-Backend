package com.icoder.core.controller;

import com.icoder.core.service.interfaces.GeneralStreamService;
import com.icoder.core.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
@Tag(name = "Stream")
@RequiredArgsConstructor
public class StreamController {

    private final GeneralStreamService streamService;
    private final SecurityUtils securityUtils;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Subscribe to real-time notifications", description = "Subscribes to real-time notifications from the server.")
    public SseEmitter subscribe() {
        Long userId = securityUtils.getCurrentUserId();
        return streamService.createEmitter(userId);
    }
}
