package com.icoder.notification.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.notification.management.dto.NotificationResponse;
import com.icoder.notification.management.entity.Notification;
import com.icoder.notification.management.enums.NotificationType;
import com.icoder.notification.management.mapper.NotificationMapper;
import com.icoder.notification.management.repository.NotificationRepository;
import com.icoder.notification.management.service.interfaces.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImp implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification createNotification(Invitation invitation, String targetName) {
        Notification notification = Notification.builder()
                .recipient(invitation.getRecipient())
                .targetId(invitation.getTargetId())
                .message(invitation.getSender().getNickname() + " invited you to join " + targetName)
                .type(NotificationType.INVITATION)
                .actionUrl("/api/v1/invitations/respond?token=" + invitation.getToken())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created: id={}, recipientId={}, type={}",
                savedNotification.getId(), invitation.getRecipient().getId(), NotificationType.INVITATION);

        return savedNotification;
    }

    @Override
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Page<Notification> notifications = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(
                currentUserId, pageable);

        log.debug("Fetched {} notifications for user {}", notifications.getTotalElements(), currentUserId);
        return notifications.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional
    public MessageResponse markAsRead(Long notificationId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("Notification not found"));

        // Authorization check: ensure the current user is the recipient
        if (!notification.getRecipient().getId().equals(currentUserId)) {
            log.warn("User {} attempted to mark notification {} belonging to user {} as read",
                    currentUserId, notificationId, notification.getRecipient().getId());
            throw new ApiException("You are not authorized to modify this notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Notification marked as read: id={}, userId={}", notificationId, currentUserId);
        }

        return MessageResponse.builder()
                .message("Notification marked as read successfully")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse markAllAsRead() {
        Long currentUserId = securityUtils.getCurrentUserId();

        int updatedCount = notificationRepository.markAllAsReadByRecipientId(currentUserId);
        log.info("Marked {} notifications as read for user {}", updatedCount, currentUserId);

        return MessageResponse.builder()
                .message(updatedCount + " notification(s) marked as read")
                .build();
    }

    @Override
    public Long getUnreadCount() {
        Long currentUserId = securityUtils.getCurrentUserId();
        Long count = notificationRepository.countByRecipientIdAndIsRead(currentUserId, false);

        log.debug("User {} has {} unread notifications", currentUserId, count);
        return count;
    }

    @Override
    @Transactional
    public MessageResponse deleteReadNotifications() {
        Long currentUserId = securityUtils.getCurrentUserId();

        notificationRepository.deleteByRecipientIdAndIsRead(currentUserId, true);
        log.info("Deleted read notifications for user {}", currentUserId);

        return MessageResponse.builder()
                .message("Read notifications deleted successfully")
                .build();
    }
}
