package com.icoder.user.management.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.core.util.TokenHelper;
import com.icoder.core.util.ValidatePasswordChange;
import com.icoder.user.management.dto.auth.*;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.AuthMapper;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final TokenServiceImpl tokenServiceImpl;
    private final EmailVerificationServiceImpl emailVerificationServiceImpl;
    private final ValidatePasswordChange validatePasswordChange;
    private final TokenHelper tokenHelper;
    private final AuthMapper authMapper;

    @Transactional
    public MessageResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(
                    "Email is already used",
                    Map.of("field", "email", "value", request.getEmail())
            );
        }
        if (userRepository.existsByHandle(request.getHandle())) {
            throw new ApiException(
                    "Handle is already taken",
                    Map.of("field", "handle", "value", request.getHandle())
            );
        }
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new ApiException(
                    "New password and confirmation password do not match",
                    Map.of("field", "password_confirmation")
            );
        }

        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        return new MessageResponse("Account created successfully! Please verify your email before logging in.");
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getHandle(), request.getPassword())
        );
        User user = userRepository.findByHandle(request.getHandle())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        String jwtToken = jwtServiceImpl.generateToken(new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        ));
        String refreshJwtToken = jwtServiceImpl.generateRefreshToken(new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        ));
        tokenServiceImpl.revokeAllUserTokens(user);
        tokenServiceImpl.saveUserToken(user, jwtToken);

        tokenServiceImpl.addTokenCookies(response, jwtToken, refreshJwtToken);

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshJwtToken)
                .build();
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String refreshToken = authHeader.substring(7);
        String userEmail = jwtServiceImpl.extractUserHandle(refreshToken);
        if (userEmail != null) {
            User user = this.userRepository.findByHandle(userEmail)
                    .orElseThrow();
            if (jwtServiceImpl.isTokenValid(refreshToken, new CustomUserDetails(
                    user.getId(),
                    user.getHandle(),
                    user.getPassword(),
                    user.isVerified()
            ))) {
                String accessToken = jwtServiceImpl.generateToken(new CustomUserDetails(
                        user.getId(),
                        user.getHandle(),
                        user.getPassword(),
                        user.isVerified()
                ));
                tokenServiceImpl.revokeAllUserTokens(user);
                tokenServiceImpl.saveUserToken(user, accessToken);

                tokenServiceImpl.addTokenCookies(response, accessToken, refreshToken);
                LoginResponse authenticationResponse = LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
            }
        }
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        if (jwtServiceImpl.isTokenExpired(token)) {
            throw new ApiException("Verification link has expired");
        }
        String handle = jwtServiceImpl.extractUserHandle(token);
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.setVerified(true);
        userRepository.save(user);
        return new MessageResponse("Email verified successfully! You can now log in");
    }

    public MessageResponse sendEmailVerification(SendVerificationEmailRequest request) {
        User user = userRepository.findByHandle(request.getHandle())
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.isVerified()) {
            throw new ApiException("Email is already verified");
        }

        emailVerificationServiceImpl.sendVerificationEmail(user);
        return new MessageResponse("Verification email sent, please check your email");
    }

    // if user forgot password call forgetPassword() then resetPassword()
    public MessageResponse forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("No user found with this email"));
        emailVerificationServiceImpl.sendPasswordResetEmail(user);
        return new MessageResponse("Password change link is sent to your email");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (jwtServiceImpl.isTokenExpired(request.getToken())) {
            throw new ApiException("Verification link has expired");
        }
        validatePasswordChange.validatePasswordChange(request);
        var result = tokenHelper.validateAndExtract(request.getToken());
        result.user().setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(result.user());
        refreshSecurityContext(result.user());
        return new MessageResponse("Password has been reset successfully");
    }

    // if he logged in
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request, Principal principal) {
        String userHandle = principal.getName();
        User user = userRepository.findByHandle(userHandle)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        validatePasswordChange.validatePasswordChange(request, user);
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        refreshSecurityContext(user);
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

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
    @Override
    public String getCurrentUserUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
