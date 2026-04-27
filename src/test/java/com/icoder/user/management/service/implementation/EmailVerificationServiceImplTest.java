package com.icoder.user.management.service.implementation;

import com.icoder.core.exception.ApiException;
import com.icoder.core.service.interfaces.MailSenderService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private MailSenderService mailSenderService;

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                emailVerificationService,
                "frontendUrl",
                "http://localhost:3000"
        );

        user = new User();
        user.setId(1L);
        user.setHandle("testHandle");
        user.setEmail("test@example.com");
        user.setNickname("Test");
        user.setPassword("hashedPassword");
        user.setVerified(true);
        user.setLastVerificationEmailSentAt(null);
    }

    @Nested
    @DisplayName("sendVerificationEmail()")
    class SendVerificationEmailTests {

        @Test
        @DisplayName("should send verification email and update timestamp")
        void sendVerificationEmail_shouldSendEmailAndUpdateTimestamp() {
            when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(), eq(15 * 60 * 1000L)))
                    .thenReturn("verification-token");

            emailVerificationService.sendVerificationEmail(user);

            verify(jwtService).generateTokenWithCustomExpiration(anyMap(), any(), eq(15 * 60 * 1000L));

            ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(mailSenderService).sendSimpleEmail(recipientCaptor.capture(), bodyCaptor.capture());
            verify(userRepository).save(user);

            assertEquals("test@example.com", recipientCaptor.getValue());

            String emailBody = bodyCaptor.getValue();
            assertTrue(emailBody.contains("Hello Test"));
            assertTrue(emailBody.contains("verify your email"));
            assertTrue(emailBody.contains("http://localhost:3000/confirm-email?token=verification-token"));
            assertTrue(emailBody.contains("15 minutes"));

            assertNotNull(user.getLastVerificationEmailSentAt());
        }

        @Test
        @DisplayName("should throw ApiException when verification email cooldown has not passed")
        void sendVerificationEmail_shouldThrowApiException_whenCooldownNotPassed() {
            user.setLastVerificationEmailSentAt(Instant.now().minus(2, ChronoUnit.MINUTES));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> emailVerificationService.sendVerificationEmail(user)
            );

            assertTrue(ex.getMessage().startsWith("Please wait"));
            verifyNoInteractions(jwtService, mailSenderService);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("sendPasswordResetEmail()")
    class SendPasswordResetEmailTests {

        @Test
        @DisplayName("should send password reset email and update timestamp")
        void sendPasswordResetEmail_shouldSendEmailAndUpdateTimestamp() {
            when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(), eq(5 * 60 * 1000L)))
                    .thenReturn("reset-token");

            emailVerificationService.sendPasswordResetEmail(user);

            verify(jwtService).generateTokenWithCustomExpiration(anyMap(), any(), eq(5 * 60 * 1000L));

            ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(mailSenderService).sendSimpleEmail(recipientCaptor.capture(), bodyCaptor.capture());
            verify(userRepository).save(user);

            assertEquals(user.getEmail(), recipientCaptor.getValue());

            String emailBody = bodyCaptor.getValue();
            assertTrue(emailBody.contains("reset your password"));
            assertTrue(emailBody.contains("http://localhost:3000/change-password?token=reset-token"));
            assertTrue(emailBody.contains("5 minutes"));

            assertNotNull(user.getLastVerificationEmailSentAt());
        }

        @Test
        @DisplayName("should throw ApiException when password reset cooldown has not passed")
        void sendPasswordResetEmail_shouldThrowApiException_whenCooldownNotPassed() {
            user.setLastVerificationEmailSentAt(Instant.now().minus(1, ChronoUnit.MINUTES));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> emailVerificationService.sendPasswordResetEmail(user)
            );

            assertTrue(ex.getMessage().startsWith("Please wait"));
            verifyNoInteractions(jwtService, mailSenderService);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("sendAccountDeletionEmail()")
    class SendAccountDeletionEmailTests {

        @Test
        @DisplayName("should send account deletion email and update timestamp")
        void sendAccountDeletionEmail_shouldSendEmailAndUpdateTimestamp() {
            when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(), eq(10 * 60 * 1000L)))
                    .thenReturn("deletion-token");

            emailVerificationService.sendAccountDeletionEmail(user);

            verify(jwtService).generateTokenWithCustomExpiration(anyMap(), any(), eq(10 * 60 * 1000L));

            ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(mailSenderService).sendSimpleEmail(recipientCaptor.capture(), bodyCaptor.capture());
            verify(userRepository).save(user);

            assertEquals(user.getEmail(), recipientCaptor.getValue());

            String emailBody = bodyCaptor.getValue();
            assertTrue(emailBody.contains("confirm deletion of your account"));
            assertTrue(emailBody.contains("http://localhost:3000/landing-page?token=deletion-token"));
            assertTrue(emailBody.contains("10 minutes"));

            assertNotNull(user.getLastVerificationEmailSentAt());
        }

        @Test
        @DisplayName("should throw ApiException when account deletion cooldown has not passed")
        void sendAccountDeletionEmail_shouldThrowApiException_whenCooldownNotPassed() {
            user.setLastVerificationEmailSentAt(Instant.now().minus(5, ChronoUnit.MINUTES));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> emailVerificationService.sendAccountDeletionEmail(user)
            );

            assertTrue(ex.getMessage().startsWith("Please wait"));
            verifyNoInteractions(jwtService, mailSenderService);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("sendEmailUpdateVerificationEmail()")
    class SendEmailUpdateVerificationEmailTests {

        @Test
        @DisplayName("should send email update verification email to new email and update timestamp")
        void sendEmailUpdateVerificationEmail_shouldSendEmailToNewEmailAndUpdateTimestamp() {
            String newEmail = "new@example.com";

            when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(), eq(15 * 60 * 1000L)))
                    .thenReturn("email-update-token");

            emailVerificationService.sendEmailUpdateVerificationEmail(user, newEmail);

            ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(jwtService).generateTokenWithCustomExpiration(claimsCaptor.capture(), any(), eq(15 * 60 * 1000L));

            Map<String, Object> claims = claimsCaptor.getValue();
            assertEquals(newEmail, claims.get("newEmail"));
            assertEquals(TokenType.EMAIL_UPDATE, claims.get("type"));

            ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

            verify(mailSenderService).sendSimpleEmail(recipientCaptor.capture(), bodyCaptor.capture());
            verify(userRepository).save(user);

            assertEquals(newEmail, recipientCaptor.getValue());

            String emailBody = bodyCaptor.getValue();
            assertTrue(emailBody.contains("update the email address of your account"));
            assertTrue(emailBody.contains("http://localhost:3000/profile?token=email-update-token"));
            assertTrue(emailBody.contains("15 minutes"));

            assertNotNull(user.getLastVerificationEmailSentAt());
        }

        @Test
        @DisplayName("should throw ApiException when email update cooldown has not passed")
        void sendEmailUpdateVerificationEmail_shouldThrowApiException_whenCooldownNotPassed() {
            user.setLastVerificationEmailSentAt(Instant.now().minus(2, ChronoUnit.MINUTES));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> emailVerificationService.sendEmailUpdateVerificationEmail(user, "new@example.com")
            );

            assertTrue(ex.getMessage().startsWith("Please wait"));
            verifyNoInteractions(jwtService, mailSenderService);
            verify(userRepository, never()).save(any());
        }
    }
}