package com.icoder.user.management.service.interfaces;

import com.icoder.user.management.entity.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user);

    void sendPasswordResetEmail(User user);

    void sendPasswordChangeVerificationEmail(User user, String encodedNewPassword);

    void sendAccountDeletionEmail(User user);

    void sendEmailUpdateVerificationEmail(User user, String newEmail);
}
