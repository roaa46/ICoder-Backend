package com.icoder.notification.management.service.implementation;

import com.icoder.notification.management.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendInvitationEmail(String to, String senderName, String targetName, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New Invitation to join " + targetName);
        message.setText("Hi! " + senderName + " has invited you to join " + targetName +
                ". \nUse this link to respond: http://yourfront.com/invite?token=" + token);

        mailSender.send(message);
    }
}
