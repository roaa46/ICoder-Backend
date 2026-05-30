package com.icoder.notification.management.events;

import com.icoder.invitation.management.entity.Invitation;
import lombok.Value;

@Value
public class InvitationSentEvent {
    Invitation invitation;
    String targetName;
}
