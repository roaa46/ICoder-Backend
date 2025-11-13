package com.icoder.user.management.controller;

import com.icoder.user.management.dto.auth.*;
import com.icoder.user.management.service.implementation.AuthenticationServiceImpl;
import com.icoder.user.management.service.implementation.LogoutServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationServiceImpl authenticationServiceImpl;
    private final LogoutServiceImpl logoutServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationServiceImpl.register(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationServiceImpl.login(request, response));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutServiceImpl.logout(request, response, authentication);
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationServiceImpl.refreshToken(request, response);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String result = authenticationServiceImpl.verifyEmail(token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        authenticationServiceImpl.forgetPassword(request);
        return ResponseEntity.ok("Password change link sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationServiceImpl.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PostMapping("change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        authenticationServiceImpl.changePassword(request, principal);
        return ResponseEntity.ok("Password changed successfully. Please verify it from your email.");
    }

    @GetMapping("/confirm-password-change")
    public ResponseEntity<String> confirmPasswordChange(@RequestParam String token) {
        authenticationServiceImpl.confirmPasswordChange(token);
        return ResponseEntity.ok("Password change confirmed.");
    }


}
