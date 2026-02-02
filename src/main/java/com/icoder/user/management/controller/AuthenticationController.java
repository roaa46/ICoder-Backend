package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.*;
import com.icoder.user.management.service.implementation.LogoutServiceImpl;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final LogoutServiceImpl logoutServiceImpl;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details and needs to be verified"
    )
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.register(request, response));
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with handle and password and sets authentication/refresh cookies."
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.login(request, response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Invalidates the user's session and clears authentication cookies."
    )
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutServiceImpl.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Uses the refresh token (sent via cookie) to generate a new access token and update the authentication cookie."
    )
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verify/send")
    @Operation(
            summary = "Send email verification link",
            description = "Sends a new email containing the verification link to the user's email address."
    )
    public ResponseEntity<MessageResponse> sendVerificationEmail(@Valid @RequestBody SendVerificationEmailRequest emailRequest) {
        return ResponseEntity.ok(authenticationService.sendEmailVerification(emailRequest));
    }

    @GetMapping("/verify")
    @Operation(
            summary = "Verify user email",
            description = "Confirms the user's email address using a verification token sent via email."
    )
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationService.verifyEmail(token));
    }

    @PostMapping("/password/forget")
    @Operation(
            summary = "Initiate password reset process",
            description = "Sends a password reset link to the user's email address."
    )
    public ResponseEntity<MessageResponse> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.forgetPassword(request));
    }

    @PostMapping("/password/reset")
    @Operation(
            summary = "Reset user password",
            description = "Resets the user's password using a valid reset token and a new password."
    )
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }

    @PatchMapping("/password")
    @Operation(
            summary = "Change authenticated user's password",
            description = "Allows an authenticated user to change their password by providing the old and new passwords."
    )
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authenticationService.changePassword(request));
    }
}
