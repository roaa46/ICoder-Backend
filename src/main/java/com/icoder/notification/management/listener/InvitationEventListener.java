package com.icoder.notification.management.listener;

import com.icoder.invitation.management.entity.Invitation;
import com.icoder.notification.management.events.InvitationSentEvent;
import com.icoder.notification.management.entity.Notification;
import com.icoder.notification.management.enums.NotificationType;
import com.icoder.notification.management.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationEventListener {
    private final NotificationService notificationService;
    // emailService: EmailService

    @Async
    @EventListener
    public void handleInvitationSentEvent(InvitationSentEvent event) {
        Invitation invitation = event.getInvitation();

        Notification notification = Notification.builder()
                .recipient(invitation.getRecipient())
                .targetId(invitation.getTargetId())
                .message(invitation.getSender().getNickname() + " invited you to join " + event.getTargetName())
                .type(NotificationType.INVITATION)
                .actionUrl("/api/v1/invitations/respond?token=" + invitation.getToken())
                .build();

        notificationService.createNotification(notification);

        // send email

        System.out.println("Async notification processed for token: " + invitation.getToken());
    }
}
