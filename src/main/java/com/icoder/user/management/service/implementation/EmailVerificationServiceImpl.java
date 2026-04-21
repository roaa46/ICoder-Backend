package com.icoder.user.management.service.implementation;

import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.core.service.interfaces.MailSenderService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.EmailVerificationService;
import com.icoder.user.management.service.interfaces.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MailSenderService mailSenderService;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    @Transactional
    @Override
    public void sendVerificationEmail(User user) {
        processEmailRequest(user, TokenType.ACCOUNT_ACTIVATION, "/confirm-email", 5, 15 * 60 * 1000);
    }

    @Transactional
    @Override
    public void sendPasswordResetEmail(User user) {
        processEmailRequest(user, TokenType.PASSWORD_RESET, "/change-password", 3, 5 * 60 * 1000);
    }

    @Transactional
    @Override
    public void sendAccountDeletionEmail(User user) {
        processEmailRequest(user, TokenType.ACCOUNT_DELETION, "/landing-page", 15, 10 * 60 * 1000);
    }

    @Transactional
    @Override
    public void sendEmailUpdateVerificationEmail(User user, String newEmail) {
        validateEmailCooldown(user, 5);

        Map<String, Object> claims = new HashMap<>();
        claims.put("newEmail", newEmail);

        String token = generateToken(user, claims, TokenType.EMAIL_UPDATE, 15 * 60 * 1000);
        String link = String.format("%s/profile?token=%s", frontendUrl, token);

        mailSenderService.sendSimpleEmail(newEmail, buildEmailBody(user.getNickname(), link, TokenType.EMAIL_UPDATE, 15));
        log.info("Email sent successfully to {}", newEmail);
        updateLastSentTimestamp(user);
    }


    private void processEmailRequest(User user, TokenType type, String path, int cooldownMins, long expiryMs) {
        validateEmailCooldown(user, cooldownMins);

        String token = generateToken(user, new HashMap<>(), type, expiryMs);
        String link = String.format("%s%s?token=%s", frontendUrl, path, token);

        long expiryMinutes = expiryMs / (60 * 1000);

        mailSenderService.sendSimpleEmail(user.getEmail(), buildEmailBody(user.getNickname(), link, type, expiryMinutes));
        log.info("Email sent successfully to {}", user.getEmail());
        updateLastSentTimestamp(user);
    }

    private String buildEmailBody(String name, String link, TokenType type, long expiryMinutes) {
        String action = switch (type) {
            case ACCOUNT_ACTIVATION -> "verify your email";
            case PASSWORD_RESET -> "reset your password";
            case ACCOUNT_DELETION -> "confirm deletion of your account";
            case EMAIL_UPDATE -> "update the email address of your account";
            default -> "complete your action";
        };

        return String.format("""
                        Hello %s,
                        
                        Please click the following link to %s:
                        %s
                        
                        This link is valid for %d minutes. For security reasons, do not share this link with anyone.
                        
                        If you did not request this, you can safely ignore this email.
                        
                        Best regards,
                        The ICoder Team""",
                name, action, link, expiryMinutes);
    }

    private void validateEmailCooldown(User user, int mins) {
        if (user.getLastVerificationEmailSentAt() != null) {
            Instant nextAllowed = user.getLastVerificationEmailSentAt().plus(mins, ChronoUnit.MINUTES);
            Instant now = Instant.now();

            if (now.isBefore(nextAllowed)) {
                long minutesLeft = Duration.between(now, nextAllowed).toMinutes();
                log.warn("Rate limit hit for user {}: {} mins remaining", user.getId(), minutesLeft);
                throw new ApiException("Please wait " + (minutesLeft == 0 ? "a moment" : minutesLeft + " minutes") + " before requesting another email.");
            }
        }
    }

    private void updateLastSentTimestamp(User user) {
        user.setLastVerificationEmailSentAt(Instant.now());
        userRepository.save(user);
    }

    private String generateToken(User user, Map<String, Object> claims, TokenType type, long expiration) {
        claims.put("type", type);
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        );
        return jwtService.generateTokenWithCustomExpiration(claims, userDetails, expiration);
    }
}