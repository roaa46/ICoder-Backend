package com.icoder.user.management.service.implementation;

import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.core.service.interfaces.MailSenderService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private User testUser;

    @BeforeEach
    void setUp() {
        String FRONTEND_URL = "http://localhost:3000";
        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", FRONTEND_URL);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("dev@example.com");
        testUser.setNickname("JavaDev");
        testUser.setHandle("jdev");
        testUser.setVerified(false);
    }

    @Test
    @DisplayName("Should send verification email successfully when no cooldown exists")
    void sendVerificationEmail_Success() {
        // Arrange
        String mockToken = "mock-jwt-token";
        when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(CustomUserDetails.class), anyLong()))
                .thenReturn(mockToken);

        // Act
        emailVerificationService.sendVerificationEmail(testUser);

        // Assert
        verify(mailSenderService).sendSimpleEmail(
                eq("dev@example.com"),
                argThat(body -> body.contains(mockToken) && body.contains("verify your email"))
        );

        verify(userRepository).save(testUser);
        assertThat(testUser.getLastVerificationEmailSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw ApiException when cooldown period is active")
    void sendVerificationEmail_CooldownActive() {
        // Arrange
        testUser.setLastVerificationEmailSentAt(Instant.now().minus(2, ChronoUnit.MINUTES));

        // Act & Assert
        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(testUser))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Please wait");

        verifyNoInteractions(mailSenderService);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should send email update verification with custom claims")
    @SuppressWarnings("unchecked")
    void sendEmailUpdateVerificationEmail_Success() {
        // Arrange
        String newEmail = "new-office@example.com";
        String mockToken = "update-token";

        when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(CustomUserDetails.class), anyLong()))
                .thenReturn(mockToken);

        // Act
        emailVerificationService.sendEmailUpdateVerificationEmail(testUser, newEmail);

        // Assert
        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jwtService).generateTokenWithCustomExpiration(claimsCaptor.capture(), any(), anyLong());

        assertThat(claimsCaptor.getValue()).containsEntry("newEmail", newEmail);
        assertThat(claimsCaptor.getValue()).containsEntry("type", TokenType.EMAIL_UPDATE);

        verify(mailSenderService).sendSimpleEmail(
                eq(newEmail),
                argThat(body -> body.contains("update the email address"))
        );
    }

    @Test
    @DisplayName("Should propagate ApiException when mail sender service fails")
    void sendEmail_WhenMailServiceThrows_ShouldPropagate() {
        // Arrange
        when(jwtService.generateTokenWithCustomExpiration(anyMap(), any(), anyLong())).thenReturn("token");

        doThrow(new ApiException("Mail server error"))
                .when(mailSenderService).sendSimpleEmail(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> emailVerificationService.sendPasswordResetEmail(testUser))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Mail server error");
    }
}