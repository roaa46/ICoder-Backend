package com.icoder.user.management.service.implementation;

import com.icoder.user.management.enums.TokenType;
import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.EmailVerificationService;
import com.icoder.user.management.service.interfaces.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    @Value("${ALLOWED_ORIGIN}")
    private String baseUrl;

    @Transactional
    @Override
    public void sendVerificationEmail(User user) {
        validateEmailCooldown(user, 5);
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.ACCOUNT_ACTIVATION);

        String token = generateToken(user, claims, 15 * 60 * 1000);

        String link = baseUrl + "/confirm-email?token=" + token;
        sendEmail(user.getEmail(), buildEmail(user.getNickname(), link, (TokenType) claims.get("type")));
        user.setLastVerificationEmailSentAt(Instant.now());
        userRepository.save(user);
    }

    private void sendEmail(String to, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText(body);
        mailSender.send(message);
    }

    private String buildEmail(String name, String link, TokenType type) {
        return switch (type) {
            case ACCOUNT_ACTIVATION ->
                    "Hello " + name + ",\nPlease click the following link to verify your email:\n" + link;
            case PASSWORD_RESET ->
                    "Hello " + name + ",\nPlease click the following link to reset your password:\n" + link;
            case ACCOUNT_DELETION ->
                    "Hello " + name + ",\nPlease click the following link to confirm deletion of your account:\n" + link;
            case EMAIL_UPDATE ->
                    "Hello " + name + ",\nPlease click the following link to update email of your account:\n" + link;
            default -> "Hello " + name + ",\nPlease click the following link:\n" + link;
        };
    }

    @Override
    public void sendPasswordResetEmail(User user) {
        validateEmailCooldown(user, 3);
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.PASSWORD_RESET);

        String token = generateToken(user, claims, 5 * 60 * 1000);

        String link = baseUrl + "/change-password?token=" + token;
        sendEmail(user.getEmail(), buildEmail(user.getNickname(), link, (TokenType) claims.get("type")));
    }

    private void validateEmailCooldown(User user, int mins) {
        if (user.getLastVerificationEmailSentAt() != null) {
            Instant now = Instant.now();
            Instant lastSent = user.getLastVerificationEmailSentAt();
            Instant nextAllowed = lastSent.plus(mins, ChronoUnit.MINUTES);
            if (now.isBefore(nextAllowed)) {
                long minutesLeft = Duration.between(now, nextAllowed).toMinutes();
                throw new ApiException("Please wait " + minutesLeft + " minutes before requesting another email.");
            }
        }
    }

    @Transactional
    @Override
    public void sendAccountDeletionEmail(User user) {
        validateEmailCooldown(user, 15);
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.ACCOUNT_DELETION);

        String token = generateToken(user, claims, 10 * 60 * 1000);

        String link = baseUrl + "/landing-page?token=" + token;
        sendEmail(user.getEmail(), buildEmail(user.getNickname(), link, (TokenType) claims.get("type")));
        user.setLastVerificationEmailSentAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public void sendEmailUpdateVerificationEmail(User user, String newEmail) {
        validateEmailCooldown(user, 5);
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.EMAIL_UPDATE);
        claims.put("newEmail", newEmail);

        String token = generateToken(user, claims, 15 * 60 * 1000);

        String link = baseUrl + "/profile?token=" + token;
        sendEmail(newEmail, buildEmail(user.getNickname(), link, (TokenType) claims.get("type")));
    }

    private String generateToken(User user, Map<String, Object> claims, long verificationExpiration) {
        Long id = user.getId();
        String handle = user.getHandle();
        String password = user.getPassword();
        boolean verified = user.isVerified();

        CustomUserDetails userDetails = new CustomUserDetails(id, handle, password, verified);
        return jwtService.generateTokenWithCustomExpiration(
                claims,
                userDetails,
                verificationExpiration
        );
    }
}
