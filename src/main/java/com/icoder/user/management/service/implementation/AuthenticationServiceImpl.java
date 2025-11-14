package com.icoder.user.management.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.core.exception.ApiException;
import com.icoder.core.exception.EmailException;
import com.icoder.core.exception.PasswordException;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;

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
    public String register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered.");
        }
        if (userRepository.existsByHandle(request.getHandle())) {
            throw new ApiException("Handle is already taken.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordException("New password and confirmation password do not match");
        }

        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now().toString());
        User savedUser = userRepository.save(user);
        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(user.getHandle(), user.getPassword(), new ArrayList<>());
        emailVerificationServiceImpl.sendVerificationEmail(savedUser);
        String jwtToken = jwtServiceImpl.generateToken(userDetails);
        String refreshJwtToken = jwtServiceImpl.generateRefreshToken(userDetails);
        tokenServiceImpl.revokeAllUserTokens(savedUser);
        tokenServiceImpl.saveUserToken(savedUser, jwtToken);

        tokenServiceImpl.addTokenCookies(response, jwtToken, refreshJwtToken);

        return "Account created successfully! Please verify your email before logging in.";
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getHandle(), request.getPassword())
        );
        User user = userRepository.findByHandle(request.getHandle())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        if (!user.isVerified()) {
            throw new EmailException("Account is not verified. Please check your email.");
        }
        String jwtToken = jwtServiceImpl.generateToken(new CustomUserDetails(user));
        String refreshJwtToken = jwtServiceImpl.generateRefreshToken(new CustomUserDetails(user));
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
            if (jwtServiceImpl.isTokenValid(refreshToken, new CustomUserDetails(user))) {
                String accessToken = jwtServiceImpl.generateToken(new CustomUserDetails(user));
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
    public String verifyEmail(String token) {
        String handle = jwtServiceImpl.extractUserHandle(token);
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.setVerified(true);
        userRepository.save(user);
        return "Email verified successfully! You can now log in.";
    }

    // if user forgot password call forgetPassword() then resetPassword()
    public void forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EmailException("No user found with this email"));
        emailVerificationServiceImpl.sendPasswordResetEmail(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validatePasswordChange.validatePasswordChange(request);
        var result = tokenHelper.validateAndExtract(request.getToken());
        result.user().setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(result.user());
    }

    // if he logged in changePassword() then confirmPasswordChange()
    public void changePassword(ChangePasswordRequest request, Principal principal) {
        String userHandle = principal.getName();
        User user = userRepository.findByHandle(userHandle)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        validatePasswordChange.validatePasswordChange(request, user);
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        emailVerificationServiceImpl.sendPasswordChangeVerificationEmail(user, encodedNewPassword);
    }

    @Transactional
    public void confirmPasswordChange(String token) {
        var result = tokenHelper.validateAndExtract(token);
        String encodedPassword = jwtServiceImpl.extractClaim(token, claims -> claims.get("newPassword").toString());
        result.user().setPassword(encodedPassword);
        userRepository.save(result.user());
    }
}
