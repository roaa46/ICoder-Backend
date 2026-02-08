package com.icoder.notification.management.listener;

import com.icoder.invitation.management.entity.Invitation;
import com.icoder.notification.management.events.InvitationSentEvent;
import com.icoder.notification.management.entity.Notification;
import com.icoder.notification.management.service.interfaces.EmailService;
import com.icoder.notification.management.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationEventListener {
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleInvitationSentEvent(InvitationSentEvent event) {
        Invitation invitation = event.getInvitation();

        Notification notification = notificationService.createNotification(invitation, event.getTargetName());

        String destination = "/topic/notifications";
        messagingTemplate.convertAndSendToUser(
                invitation.getRecipient().getId().toString(),
                destination,
                notification
        );

        emailService.sendInvitationEmail(
                invitation.getRecipient().getEmail(),
                invitation.getSender().getNickname(),
                event.getTargetName(),
                invitation.getToken()
        );

        System.out.println("Async notification processed for token: " + invitation.getToken());
    }
}
