package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
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
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationServiceImpl.register(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authenticationServiceImpl.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutServiceImpl.logout(request, response, authentication);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully."));
    }

    @PostMapping("refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationServiceImpl.refreshToken(request, response);
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authenticationServiceImpl.verifyEmail(token));
    }

    @PostMapping("/verify/send")
    public ResponseEntity<MessageResponse> sendVerificationEmail(@RequestBody SendVerificationEmailRequest emailRequest) {
        return ResponseEntity.ok(authenticationServiceImpl.sendEmailVerification(emailRequest));
    }

    @PostMapping("/password/forget")
    public ResponseEntity<MessageResponse> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        return ResponseEntity.ok(authenticationServiceImpl.forgetPassword(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authenticationServiceImpl.resetPassword(request));
    }

    @PatchMapping("password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        return ResponseEntity.ok(authenticationServiceImpl.changePassword(request, principal));
    }

}
