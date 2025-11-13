package com.icoder.user.management.service.interfaces;

import com.icoder.user.management.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;

public interface AuthenticationService {
    String register(RegisterRequest request, HttpServletResponse response);

    LoginResponse login(LoginRequest request, HttpServletResponse response);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    String verifyEmail(String token);

    void forgetPassword(ForgetPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request, Principal principal);

    void confirmPasswordChange(String token);
}
