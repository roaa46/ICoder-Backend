package com.icoder.invitation.management.utils;

import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.enums.InvitationStatus;
import com.icoder.invitation.management.enums.InvitationType;
import com.icoder.user.management.entity.User;

import java.time.Instant;
import java.util.UUID;

public class InvitationUtils {
    // 24 hours in seconds
    public static final long DEFAULT_INVITATION_EXPIRY_SECONDS = 86400;

    public static Invitation groupInvitationBuilder(Long groupId, User sender, User recipient) {
        String token = UUID.randomUUID().toString();
        return Invitation.builder()
                .type(InvitationType.GROUP)
                .targetId(groupId)
                .sender(sender)
                .recipient(recipient)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(DEFAULT_INVITATION_EXPIRY_SECONDS))
                .status(InvitationStatus.PENDING)
                .build();
    }
}
