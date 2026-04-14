package com.icoder.notification.management.service.implementation;

import com.icoder.core.config.EmailProperties;
import com.icoder.notification.management.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendInvitationEmail(String to, String senderName, String targetName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getSenderEmail());
            message.setTo(to);
            message.setSubject("New Invitation to join " + targetName);
            message.setText(buildInvitationEmailBody(senderName, targetName, token));

            mailSender.send(message);
            log.info("Invitation email sent successfully to: *** for invitation token: {}", token);
        } catch (MailException e) {
            log.error("Failed to send invitation email: {}", e.getMessage(), e);
            // Don't rethrow - we don't want email failures to break the invitation flow
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
        }
    }

    private String buildInvitationEmailBody(String senderName, String targetName, String token) {
        return String.format(
                """
                        Hi!
                        
                        %s has invited you to join %s.
                        
                        Click the link below to respond to this invitation:
                        %s/invite?token=%s
                        
                        This invitation will expire in %d hours.
                        
                        If you didn't expect this invitation, you can safely ignore this email.
                        
                        Best regards,
                        The ICoder Team""",
                senderName, targetName, emailProperties.getFrontendUrl(), token,
                emailProperties.getInvitationExpirationHours()
        );
    }
}
