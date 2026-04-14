package com.icoder.notification.management.listener;

import com.icoder.invitation.management.entity.Invitation;
import com.icoder.notification.management.dto.NotificationResponse;
import com.icoder.notification.management.events.InvitationSentEvent;
import com.icoder.notification.management.entity.Notification;
import com.icoder.notification.management.mapper.NotificationMapper;
import com.icoder.notification.management.service.interfaces.EmailService;
import com.icoder.notification.management.service.interfaces.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationEventListener {
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    @Async
    @EventListener
    @Transactional
    public void handleInvitationSentEvent(InvitationSentEvent event) {
        try {
            Invitation invitation = event.getInvitation();

            // Create notification in database
            Notification notification = notificationService.createNotification(invitation, event.getTargetName());

            NotificationResponse notificationResponse = notificationMapper.toDTO(notification);

            // Send real-time notification via WebSocket
            String destination = "/topic/notifications";
            messagingTemplate.convertAndSendToUser(
                    invitation.getRecipient().getId().toString(),
                    destination,
                    notificationResponse
            );
            log.info("WebSocket notification sent to user {}", invitation.getRecipient().getId());

            // Send email notification
            emailService.sendInvitationEmail(
                    invitation.getRecipient().getEmail(),
                    invitation.getSender().getNickname(),
                    event.getTargetName(),
                    invitation.getToken()
            );
            log.info("Email notification sent to {}", invitation.getRecipient().getEmail());

            log.info("Async invitation notification processed successfully for token: {}", invitation.getToken());
        } catch (Exception e) {
            log.error("Error processing invitation event: {}", e.getMessage(), e);
        }
    }
}
