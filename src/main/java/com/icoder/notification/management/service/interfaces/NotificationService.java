package com.icoder.notification.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.notification.management.dto.NotificationResponse;
import com.icoder.notification.management.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Notification createNotification(Invitation invitation, String targetName);
    Page<NotificationResponse> getMyNotifications(Pageable pageable);
    MessageResponse markAsRead(Long notificationId);
}
