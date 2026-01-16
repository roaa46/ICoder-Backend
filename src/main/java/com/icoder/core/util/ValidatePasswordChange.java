package com.icoder.core.util;

import com.icoder.core.exception.ApiException;
import com.icoder.user.management.dto.auth.ChangePasswordRequest;
import com.icoder.user.management.dto.auth.RegisterRequest;
import com.icoder.user.management.dto.auth.ResetPasswordRequest;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ValidatePasswordChange {
    private final PasswordEncoder passwordEncoder;

    public void validatePasswordChange(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(
                    "Current password is incorrect",
                    Map.of("field", "current_password")
            );
        }
        if (!request.getNewPassword().equals(request.getPasswordConfirmation())) {
            throw new ApiException(
                    "New password and confirmation password do not match",
                    Map.of("field", "password_confirmation")
            );
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
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
