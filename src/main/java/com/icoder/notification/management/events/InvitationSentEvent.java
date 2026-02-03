package com.icoder.notification.management.events;

import com.icoder.invitation.management.entity.Invitation;
import lombok.Getter;

@Getter
public class InvitationSentEvent {
    private final Invitation invitation;
    private final String targetName;

    public InvitationSentEvent(Invitation invitation, String targetName) {
        this.invitation = invitation;
        this.targetName = targetName;
    }
}