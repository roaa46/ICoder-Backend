package com.icoder.notification.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
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
        return notificationRepository.save(notification);
    }

    @Override
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Page<Notification> notification = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(
                securityUtils.getCurrentUserId(), pageable);
        return notification.map(notificationMapper::toDTO);
    }

    @Override
    @Transactional
    public MessageResponse markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(n -> n.setRead(true));
        return new MessageResponse("Notification marked as read successfully");
    }
}
