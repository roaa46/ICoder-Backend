package com.icoder.user.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthenticationService {
    MessageResponse register(RegisterRequest request, HttpServletResponse response);

    LoginResponse login(LoginRequest request, HttpServletResponse response);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    MessageResponse verifyEmail(String token);

    MessageResponse sendEmailVerification(SendVerificationEmailRequest request);

    MessageResponse forgetPassword(ForgetPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(ChangePasswordRequest request);
}
