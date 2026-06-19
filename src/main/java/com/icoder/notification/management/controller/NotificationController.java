package com.icoder.notification.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.notification.management.dto.NotificationResponse;
import com.icoder.notification.management.service.interfaces.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my notifications", description = "Retrieve a list of notifications for the current user.")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count", description = "Retrieve the count of unread notifications for the current user.")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a notification as read", description = "Mark a specific notification as read.")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read.")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        return ResponseEntity.ok(notificationService.markAllAsRead());
    }

    @DeleteMapping("/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete read notifications", description = "Delete all read notifications for the current user.")
    public ResponseEntity<MessageResponse> deleteReadNotifications() {
        return ResponseEntity.ok(notificationService.deleteReadNotifications());
    }
}