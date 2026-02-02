package com.icoder.invitation.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;

public interface InvitationService {
    MessageResponse respondToGroupInvitation(RespondToInvitationRequest request);
}
