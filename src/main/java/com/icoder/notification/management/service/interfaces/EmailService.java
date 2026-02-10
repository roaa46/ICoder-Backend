package com.icoder.notification.management.service.interfaces;

public interface EmailService {
    void sendInvitationEmail(String to, String senderName, String targetName, String token);
}
