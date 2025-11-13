package com.icoder.core.util;

import com.icoder.core.exception.PasswordException;
import com.icoder.user.management.dto.auth.ChangePasswordRequest;
import com.icoder.user.management.dto.auth.ResetPasswordRequest;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidatePasswordChange {
    private final PasswordEncoder passwordEncoder;

    public void validatePasswordChange(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordException("New password and confirmation password do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new PasswordException("New password must be different from current password");
        }
    }

    public void validatePasswordChange(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new PasswordException("New password and confirmation password do not match");
        }
    }
}
