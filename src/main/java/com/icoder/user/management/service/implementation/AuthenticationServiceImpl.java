package com.icoder.user.management.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.core.utils.TokenHelper;
import com.icoder.core.utils.ValidatePasswordChange;
import com.icoder.user.management.dto.auth.*;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.AuthMapper;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import com.icoder.user.management.service.interfaces.EmailVerificationService;
import com.icoder.user.management.service.interfaces.JwtService;
import com.icoder.user.management.service.interfaces.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final EmailVerificationService emailVerificationService;
    private final ValidatePasswordChange validatePasswordChange;
    private final TokenHelper tokenHelper;
    private final AuthMapper authMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    @Override
    public MessageResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Email already exists: {}", request.getEmail());
            throw new ApiException(
                    "Email is already used",
                    Map.of("field", "email", "value", request.getEmail())
            );
        }
        if (userRepository.existsByHandle(request.getHandle())) {
            log.error("Handle already exists: {}", request.getHandle());
            throw new ApiException(
                    "Handle is already taken",
                    Map.of("field", "handle", "value", request.getHandle())
            );
        }
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            log.error("New password and confirmation password do not match");
            throw new ApiException(
                    "New password and confirmation password do not match",
                    Map.of("field", "password_confirmation")
            );
        }

        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        log.info("User registered successfully: {}", user.getHandle());
        return new MessageResponse("Account created successfully! Please verify your email before logging in.");
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getHandle(), request.getPassword())
        );
        User user = userRepository.findByHandle(request.getHandle())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String jwtToken = jwtService.generateToken(new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        ));
        String refreshJwtToken = jwtService.generateRefreshToken(new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        ));
        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, jwtToken);

        tokenService.addTokenCookies(response, jwtToken, refreshJwtToken);

        log.info("User logged in successfully: {} with id: {}", user.getHandle(), user.getId());
        return LoginResponse.builder()
                .userId(user.getId())
                .accessToken(jwtToken)
                .refreshToken(refreshJwtToken)
                .build();
    }

    @Transactional
    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String refreshToken = authHeader.substring(7);
        String userEmail = jwtService.extractUserHandle(refreshToken);
        if (userEmail != null) {
            User user = this.userRepository.findByHandle(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, new CustomUserDetails(
                    user.getId(),
                    user.getHandle(),
                    user.getPassword(),
                    user.isVerified()
            ))) {
                String accessToken = jwtService.generateToken(new CustomUserDetails(
                        user.getId(),
                        user.getHandle(),
                        user.getPassword(),
                        user.isVerified()
                ));
                tokenService.revokeAllUserTokens(user);
                tokenService.saveUserToken(user, accessToken);

                tokenService.addTokenCookies(response, accessToken, refreshToken);
                LoginResponse authenticationResponse = LoginResponse.builder()
                        .userId(user.getId())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
            }
        }
    }

    @Transactional
    @Override
    public MessageResponse verifyEmail(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new ApiException("Verification link has expired");
        }
        String handle = jwtService.extractUserHandle(token);
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.setVerified(true);
        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getHandle());
        return new MessageResponse("Email verified successfully! You can now log in");
    }

    @Override
    public MessageResponse sendEmailVerification(SendVerificationEmailRequest request) {
        User user = userRepository.findByHandle(request.getHandle())
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.isVerified()) {
            log.warn("User {} attempted to resend verification email", user.getHandle());
            throw new ApiException("Email is already verified");
        }

        emailVerificationService.sendVerificationEmail(user);
        log.info("Verification email sent to user: {}", user.getHandle());
        return new MessageResponse("Verification email sent, please check your email");
    }

    // if user forgot password call forgetPassword() then resetPassword()
    @Override
    public MessageResponse forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("No user found with this email"));
        emailVerificationService.sendPasswordResetEmail(user);
        log.info("Password reset link sent to user: {}", user.getHandle());
        return new MessageResponse("Password change link is sent to your email");
    }

    @Transactional
    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (jwtService.isTokenExpired(request.getToken())) {
            log.error("Password reset token has expired");
            throw new ApiException("Verification link has expired");
        }
        validatePasswordChange.validatePasswordChange(request);
        TokenHelper.ValidatedTokenResult result = tokenHelper.validateAndExtract(request.getToken());
        result.user().setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(result.user());
        refreshSecurityContext(result.user());
        log.info("Password reset successfully for user: {}", result.user().getHandle());
        return new MessageResponse("Password has been reset successfully");
    }

    // if he logged in
    @Transactional
    @Override
    public MessageResponse changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        validatePasswordChange.validatePasswordChange(request, user);
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        refreshSecurityContext(user);
        log.info("Password changed successfully for user: {}", user.getHandle());
        return new MessageResponse("Password changed successfully");
    }

    private void refreshSecurityContext(User user) {
        CustomUserDetails updatedDetails = new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        );

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedDetails,
                null,
                updatedDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
