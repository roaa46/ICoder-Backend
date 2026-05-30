package com.icoder.user.management.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.core.utils.TokenHelper;
import com.icoder.core.utils.ValidatePasswordChange;
import com.icoder.user.management.dto.auth.*;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.AuthMapper;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.EmailVerificationService;
import com.icoder.user.management.service.interfaces.JwtService;
import com.icoder.user.management.service.interfaces.TokenService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ValidatePasswordChange validatePasswordChange;
    @Mock
    private TokenHelper tokenHelper;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerReq;
    private User user;
    private LoginRequest loginReq;
    private SendVerificationEmailRequest sendVerificationEmailReq;
    private ForgetPasswordRequest forgetPasswordReq;
    private ResetPasswordRequest resetPasswordReq;
    private ChangePasswordRequest changePasswordReq;

    @BeforeEach
    void setUp() {
        registerReq = new RegisterRequest();
        registerReq.setEmail("test@example.com");
        registerReq.setHandle("test");
        registerReq.setPassword("password");
        registerReq.setPasswordConfirmation("password");

        loginReq = new LoginRequest();
        loginReq.setHandle("test");
        loginReq.setPassword("password");

        sendVerificationEmailReq = new SendVerificationEmailRequest();
        sendVerificationEmailReq.setHandle("test");

        forgetPasswordReq = new ForgetPasswordRequest();
        forgetPasswordReq.setEmail("test@example.com");

        resetPasswordReq = new ResetPasswordRequest();
        resetPasswordReq.setToken("valid-token");
        resetPasswordReq.setNewPassword("newPassword123");
        resetPasswordReq.setConfirmationPassword("newPassword123");

        changePasswordReq = new ChangePasswordRequest();
        changePasswordReq.setCurrentPassword("oldPassword");
        changePasswordReq.setNewPassword("newPassword123");
        changePasswordReq.setPasswordConfirmation("newPassword123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setHandle("test");
        user.setPassword("hashedPassword");
        user.setVerified(true);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {
        @Test
        @DisplayName("should throw ApiException when email is already used")
        void register_shouldThrowException_whenEmailAlreadyUsed() {
            when(userRepository.existsByEmail(registerReq.getEmail())).thenReturn(true);
            ApiException ex = assertThrows(ApiException.class, () -> authenticationService.register(registerReq, response));
            // if test success (expected = actual), it will throw ApiException
            assertEquals("Email is already used", ex.getMessage());
            // check if userRepository.existsByEmail() was called only once
            verify(userRepository).existsByEmail(registerReq.getEmail());
            // check if userRepository.save() was never called
            verify(userRepository, never()).save(any());
            verifyNoInteractions(authMapper, passwordEncoder, tokenService, jwtService, authenticationManager);
        }

        @Test
        @DisplayName("should throw ApiException when handle is already taken")
        void register_shouldThrowException_whenHandleAlreadyTaken() {
            when(userRepository.existsByEmail(registerReq.getEmail())).thenReturn(false);
            when(userRepository.existsByHandle(registerReq.getHandle())).thenReturn(true);
            ApiException ex = assertThrows(ApiException.class, () -> authenticationService.register(registerReq, response));
            assertEquals("Handle is already taken", ex.getMessage());
            verify(userRepository).existsByEmail(registerReq.getEmail());
            verify(userRepository).existsByHandle(registerReq.getHandle());
            verify(userRepository, never()).save(any());
            verifyNoInteractions(authMapper, passwordEncoder, tokenService, jwtService, authenticationManager);
        }

        @Test
        @DisplayName("should throw ApiException when passwords do not match")
        void register_shouldThrowException_whenPasswordsDoNotMatch() {
            registerReq.setPasswordConfirmation("differentPassword");

            when(userRepository.existsByEmail(registerReq.getEmail())).thenReturn(false);
            when(userRepository.existsByHandle(registerReq.getHandle())).thenReturn(false);

            ApiException ex = assertThrows(ApiException.class, () -> authenticationService.register(registerReq, response));
            assertEquals("New password and confirmation password do not match", ex.getMessage());

            verify(userRepository).existsByEmail(registerReq.getEmail());
            verify(userRepository).existsByHandle(registerReq.getHandle());
            verify(userRepository, never()).save(any());
            verifyNoInteractions(authMapper, passwordEncoder, tokenService, jwtService, authenticationManager);
        }

        @Test
        @DisplayName("should save user when all conditions are met")
        void register_shouldSaveUser_whenAllConditionsAreMet() {
            when(userRepository.existsByEmail(registerReq.getEmail())).thenReturn(false);
            when(userRepository.existsByHandle(registerReq.getHandle())).thenReturn(false);
            when(passwordEncoder.encode(registerReq.getPassword())).thenReturn("hashedPassword");
            when(authMapper.toEntity(registerReq)).thenReturn(user);

            MessageResponse result = authenticationService.register(registerReq, response);

            assertEquals(
                    "Account created successfully! Please verify your email before logging in.",
                    result.getMessage()
            );

            verify(userRepository).existsByEmail(registerReq.getEmail());
            verify(userRepository).existsByHandle(registerReq.getHandle());
            verify(authMapper).toEntity(registerReq);
            verify(passwordEncoder).encode(registerReq.getPassword());
            verify(userRepository).save(user);

            assertEquals("hashedPassword", user.getPassword());
            assertNotNull(user.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {
        @Test
        @DisplayName("should login successfully and return tokens")
        void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
            String accessToken = "access-token";
            String refreshToken = "refresh-token";

            when(userRepository.findByHandle(loginReq.getHandle())).thenReturn(Optional.of(user));
            when(jwtService.generateToken(any())).thenReturn(accessToken);
            when(jwtService.generateRefreshToken(any())).thenReturn(refreshToken);

            LoginResponse result = authenticationService.login(loginReq, response);

            assertNotNull(result);
            assertEquals(user.getId(), result.getUserId());
            assertEquals(accessToken, result.getAccessToken());
            assertEquals(refreshToken, result.getRefreshToken());

            ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

            verify(authenticationManager).authenticate(authCaptor.capture());

            UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
            assertEquals(loginReq.getHandle(), authToken.getPrincipal());
            assertEquals(loginReq.getPassword(), authToken.getCredentials());

            verify(userRepository).findByHandle(loginReq.getHandle());
            verify(jwtService).generateToken(any());
            verify(jwtService).generateRefreshToken(any());
            verify(tokenService).revokeAllUserTokens(user);
            verify(tokenService).saveUserToken(user, accessToken);
            verify(tokenService).addTokenCookies(response, accessToken, refreshToken);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when authenticated user handle does not exist")
        void login_shouldThrowException_whenUserNotFound() {
            when(userRepository.findByHandle(loginReq.getHandle())).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () -> authenticationService.login(loginReq, response));
            assertEquals("User not found", ex.getMessage());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByHandle(loginReq.getHandle());
            verify(jwtService, never()).generateToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("should propagate exception when authentication fails")
        void login_shouldThrowException_whenAuthenticationFails() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            BadCredentialsException ex = assertThrows(
                    BadCredentialsException.class,
                    () -> authenticationService.login(loginReq, response)
            );

            assertEquals("Bad credentials", ex.getMessage());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByHandle(anyString());
            verify(jwtService, never()).generateToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("should return when authorization header is null")
        void refreshToken_shouldReturn_whenAuthorizationHeaderIsNull() throws IOException {
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

            authenticationService.refreshToken(request, response);

            verify(request).getHeader(HttpHeaders.AUTHORIZATION);
            verifyNoInteractions(jwtService);
            verify(userRepository, never()).findByHandle(anyString());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
            verify(response, never()).getOutputStream();
        }

        @Test
        @DisplayName("should return when authorization header does not start with Bearer")
        void refreshToken_shouldReturn_whenAuthorizationHeaderIsInvalid() throws IOException {
            when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc123");

            authenticationService.refreshToken(request, response);

            verify(request).getHeader(HttpHeaders.AUTHORIZATION);
            verifyNoInteractions(jwtService);
            verify(userRepository, never()).findByHandle(anyString());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
            verify(response, never()).getOutputStream();
        }

        @Test
        @DisplayName("should do nothing when extracted user handle is null")
        void refreshToken_shouldDoNothing_whenExtractedUserHandleIsNull() throws IOException {
            String refreshToken = "refresh-token";

            when(request.getHeader(HttpHeaders.AUTHORIZATION))
                    .thenReturn("Bearer " + refreshToken);
            when(jwtService.extractUserHandle(refreshToken)).thenReturn(null);

            authenticationService.refreshToken(request, response);

            verify(jwtService).extractUserHandle(refreshToken);
            verify(userRepository, never()).findByHandle(anyString());
            verify(jwtService, never()).isTokenValid(anyString(), any());
            verify(jwtService, never()).generateToken(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
            verify(response, never()).getOutputStream();
        }

        @Test
        @DisplayName("should throw exception when user is not found")
        void refreshToken_shouldThrowException_whenUserNotFound() {
            String refreshToken = "refresh-token";

            when(request.getHeader(HttpHeaders.AUTHORIZATION))
                    .thenReturn("Bearer " + refreshToken);
            when(jwtService.extractUserHandle(refreshToken)).thenReturn(loginReq.getHandle());
            when(userRepository.findByHandle(loginReq.getHandle())).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class,
                    () -> authenticationService.refreshToken(request, response));

            verify(jwtService).extractUserHandle(refreshToken);
            verify(userRepository).findByHandle(loginReq.getHandle());
            verify(jwtService, never()).isTokenValid(anyString(), any());
            verify(jwtService, never()).generateToken(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("should do nothing when refresh token is invalid")
        void refreshToken_shouldDoNothing_whenTokenIsInvalid() throws IOException {
            String refreshToken = "refresh-token";

            when(request.getHeader(HttpHeaders.AUTHORIZATION))
                    .thenReturn("Bearer " + refreshToken);
            when(jwtService.extractUserHandle(refreshToken)).thenReturn(user.getHandle());
            when(userRepository.findByHandle(user.getHandle())).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid(eq(refreshToken), any())).thenReturn(false);

            authenticationService.refreshToken(request, response);

            verify(jwtService).extractUserHandle(refreshToken);
            verify(userRepository).findByHandle(user.getHandle());
            verify(jwtService).isTokenValid(eq(refreshToken), any());
            verify(jwtService, never()).generateToken(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
            verify(tokenService, never()).saveUserToken(any(), anyString());
            verify(tokenService, never()).addTokenCookies(any(), anyString(), anyString());
            verify(response, never()).getOutputStream();
        }

        @Test
        @DisplayName("should generate new access token and write response when refresh token is valid")
        void refreshToken_shouldGenerateNewAccessTokenAndWriteResponse_whenTokenIsValid() throws IOException {
            String refreshToken = "refresh-token";
            String newAccessToken = "new-access-token";

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ServletOutputStream servletOutputStream = new TestServletOutputStream(byteArrayOutputStream);

            when(request.getHeader(HttpHeaders.AUTHORIZATION))
                    .thenReturn("Bearer " + refreshToken);
            when(jwtService.extractUserHandle(refreshToken)).thenReturn(user.getHandle());
            when(userRepository.findByHandle(user.getHandle())).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid(eq(refreshToken), any())).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn(newAccessToken);
            when(response.getOutputStream()).thenReturn(servletOutputStream);

            authenticationService.refreshToken(request, response);

            verify(jwtService).extractUserHandle(refreshToken);
            verify(userRepository).findByHandle(user.getHandle());
            verify(jwtService).isTokenValid(eq(refreshToken), any());
            verify(jwtService).generateToken(any());
            verify(tokenService).revokeAllUserTokens(user);
            verify(tokenService).saveUserToken(user, newAccessToken);
            verify(tokenService).addTokenCookies(response, newAccessToken, refreshToken);
            verify(response).getOutputStream();

            String json = byteArrayOutputStream.toString(StandardCharsets.UTF_8);

            LoginResponse actualResponse = new ObjectMapper().readValue(json, LoginResponse.class);

            assertEquals(user.getId(), actualResponse.getUserId());
            assertEquals(newAccessToken, actualResponse.getAccessToken());
            assertEquals(refreshToken, actualResponse.getRefreshToken());
        }

        private static class TestServletOutputStream extends ServletOutputStream {
            private final ByteArrayOutputStream outputStream;

            private TestServletOutputStream(ByteArrayOutputStream outputStream) {
                this.outputStream = outputStream;
            }

            @Override
            public void write(int b) {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // no-op
            }
        }
    }

    @Nested
    @DisplayName("verifyEmail()")
    class VerifyEmailTests {

        @Test
        @DisplayName("should throw ApiException when token is expired")
        void verifyEmail_shouldThrowApiException_whenTokenIsExpired() {
            String token = "expired-token";

            when(jwtService.isTokenExpired(token)).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> authenticationService.verifyEmail(token)
            );

            assertEquals("Verification link has expired", ex.getMessage());

            verify(jwtService).isTokenExpired(token);
            verify(jwtService, never()).extractUserHandle(anyString());
            verify(userRepository, never()).findByHandle(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when token handle is invalid")
        void verifyEmail_shouldThrowIllegalArgumentException_whenUserNotFound() {
            String token = "valid-token";
            String handle = "test";

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(jwtService.extractUserHandle(token)).thenReturn(handle);
            when(userRepository.findByHandle(handle)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> authenticationService.verifyEmail(token)
            );

            assertEquals("Invalid token", ex.getMessage());

            verify(jwtService).isTokenExpired(token);
            verify(jwtService).extractUserHandle(token);
            verify(userRepository).findByHandle(handle);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should verify email and save user when token is valid")
        void verifyEmail_shouldVerifyEmailAndSaveUser_whenTokenIsValid() {
            String token = "valid-token";
            String handle = user.getHandle();

            user.setVerified(false);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(jwtService.extractUserHandle(token)).thenReturn(handle);
            when(userRepository.findByHandle(handle)).thenReturn(Optional.of(user));

            MessageResponse result = authenticationService.verifyEmail(token);

            assertNotNull(result);
            assertEquals("Email verified successfully! You can now log in", result.getMessage());
            assertTrue(user.isVerified());

            verify(jwtService).isTokenExpired(token);
            verify(jwtService).extractUserHandle(token);
            verify(userRepository).findByHandle(handle);
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("sendEmailVerification()")
    class SendEmailVerificationTests {

        @Test
        @DisplayName("should throw ApiException when user is not found")
        void sendEmailVerification_shouldThrowApiException_whenUserNotFound() {
            when(userRepository.findByHandle(sendVerificationEmailReq.getHandle()))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> authenticationService.sendEmailVerification(sendVerificationEmailReq)
            );

            assertEquals("User not found", ex.getMessage());

            verify(userRepository).findByHandle(sendVerificationEmailReq.getHandle());
            verifyNoInteractions(emailVerificationService);
        }

        @Test
        @DisplayName("should throw ApiException when email is already verified")
        void sendEmailVerification_shouldThrowApiException_whenEmailAlreadyVerified() {
            user.setVerified(true);

            when(userRepository.findByHandle(sendVerificationEmailReq.getHandle()))
                    .thenReturn(Optional.of(user));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> authenticationService.sendEmailVerification(sendVerificationEmailReq)
            );

            assertEquals("Email is already verified", ex.getMessage());

            verify(userRepository).findByHandle(sendVerificationEmailReq.getHandle());
            verifyNoInteractions(emailVerificationService);
        }

        @Test
        @DisplayName("should send verification email when user exists and is not verified")
        void sendEmailVerification_shouldSendVerificationEmail_whenUserIsNotVerified() {
            user.setVerified(false);

            when(userRepository.findByHandle(sendVerificationEmailReq.getHandle()))
                    .thenReturn(Optional.of(user));

            MessageResponse result = authenticationService.sendEmailVerification(sendVerificationEmailReq);

            assertNotNull(result);
            assertEquals("Verification email sent, please check your email", result.getMessage());

            verify(userRepository).findByHandle(sendVerificationEmailReq.getHandle());
            verify(emailVerificationService).sendVerificationEmail(user);
        }
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        @Test
        @DisplayName("should throw ApiException when reset token is expired")
        void resetPassword_shouldThrowApiException_whenTokenIsExpired() {
            when(jwtService.isTokenExpired(resetPasswordReq.getToken())).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> authenticationService.resetPassword(resetPasswordReq)
            );

            assertEquals("Verification link has expired", ex.getMessage());

            verify(jwtService).isTokenExpired(resetPasswordReq.getToken());
            verifyNoInteractions(validatePasswordChange, tokenHelper, passwordEncoder);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should validate password change before extracting token")
        void resetPassword_shouldCallValidatePasswordChange_whenTokenIsNotExpired() {
            when(jwtService.isTokenExpired(resetPasswordReq.getToken())).thenReturn(false);

            assertThrows(
                    RuntimeException.class,
                    () -> {
                        doThrow(new RuntimeException("validation failed"))
                                .when(validatePasswordChange)
                                .validatePasswordChange(resetPasswordReq);

                        authenticationService.resetPassword(resetPasswordReq);
                    }
            );

            verify(jwtService).isTokenExpired(resetPasswordReq.getToken());
            verify(validatePasswordChange).validatePasswordChange(resetPasswordReq);
            verifyNoInteractions(tokenHelper, passwordEncoder);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reset password and save user when token is valid")
        void resetPassword_shouldResetPasswordAndSaveUser_whenTokenIsValid() {
            String encodedPassword = "encoded-new-password";

            when(jwtService.isTokenExpired(resetPasswordReq.getToken())).thenReturn(false);
            doNothing().when(validatePasswordChange).validatePasswordChange(resetPasswordReq);

            TokenHelper.ValidatedTokenResult tokenResult = mock(TokenHelper.ValidatedTokenResult.class);

            when(tokenHelper.validateAndExtract(resetPasswordReq.getToken())).thenReturn(tokenResult);
            when(tokenResult.user()).thenReturn(user);
            when(passwordEncoder.encode(resetPasswordReq.getNewPassword())).thenReturn(encodedPassword);

            MessageResponse result = authenticationService.resetPassword(resetPasswordReq);

            assertNotNull(result);
            assertEquals("Password has been reset successfully", result.getMessage());
            assertEquals(encodedPassword, user.getPassword());

            verify(jwtService).isTokenExpired(resetPasswordReq.getToken());
            verify(validatePasswordChange).validatePasswordChange(resetPasswordReq);
            verify(tokenHelper).validateAndExtract(resetPasswordReq.getToken());
            verify(passwordEncoder).encode(resetPasswordReq.getNewPassword());
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("should throw UsernameNotFoundException when current user is not found")
        void changePassword_shouldThrowUsernameNotFoundException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> authenticationService.changePassword(changePasswordReq)
            );

            assertEquals("User not found", ex.getMessage());

            verify(securityUtils).getCurrentUserId();
            verify(userRepository).findById(1L);
            verifyNoInteractions(validatePasswordChange, passwordEncoder);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should validate password change before encoding and saving")
        void changePassword_shouldValidateBeforeEncodingAndSaving() {
            when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            doThrow(new ApiException("Current password is incorrect"))
                    .when(validatePasswordChange)
                    .validatePasswordChange(changePasswordReq, user);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> authenticationService.changePassword(changePasswordReq)
            );

            assertEquals("Current password is incorrect", ex.getMessage());

            verify(securityUtils).getCurrentUserId();
            verify(userRepository).findById(user.getId());
            verify(validatePasswordChange).validatePasswordChange(changePasswordReq, user);
            verifyNoInteractions(passwordEncoder);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should change password and save user when request is valid")
        void changePassword_shouldChangePasswordAndSaveUser_whenRequestIsValid() {
            String encodedNewPassword = "encodedNewPassword";

            when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            doNothing().when(validatePasswordChange).validatePasswordChange(changePasswordReq, user);
            when(passwordEncoder.encode(changePasswordReq.getNewPassword())).thenReturn(encodedNewPassword);

            MessageResponse result = authenticationService.changePassword(changePasswordReq);

            assertNotNull(result);
            assertEquals("Password changed successfully", result.getMessage());
            assertEquals(encodedNewPassword, user.getPassword());

            verify(securityUtils).getCurrentUserId();
            verify(userRepository).findById(user.getId());
            verify(validatePasswordChange).validatePasswordChange(changePasswordReq, user);
            verify(passwordEncoder).encode(changePasswordReq.getNewPassword());
            verify(userRepository).save(user);
        }
    }
}