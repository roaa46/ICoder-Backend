package com.icoder.core.utils;

import com.icoder.core.exception.ApiException;
import com.icoder.user.management.dto.auth.ChangePasswordRequest;
import com.icoder.user.management.dto.auth.ResetPasswordRequest;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidatePasswordChange {
    private final PasswordEncoder passwordEncoder;

    public void validatePasswordChange(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.error("Current password is incorrect for user: {}", user.getHandle());
            throw new ApiException(
                    "Current password is incorrect",
                    Map.of("field", "current_password")
            );
        }
        if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
            log.error("New password and confirmation password do not match for user: {}", user.getHandle());
            throw new ApiException(
                    "New password and confirmation password do not match",
                    Map.of("field", "password_confirmation")
            );
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.error("New password must be different from current password for user: {}", user.getHandle());
            throw new ApiException(
                    "New password must be different from current password",
                    Map.of(

                            "field1", "current_password",
                            "field2", "new_password"
                    )
            );
        }
    }

    public void validatePasswordChange(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new ApiException(
                    "New password and confirmation password do not match",
                    Map.of("field", "password_confirmation")
            );
        }
    }
}