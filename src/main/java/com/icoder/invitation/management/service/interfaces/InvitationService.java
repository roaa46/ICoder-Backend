package com.icoder.invitation.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;

public interface InvitationService {
    MessageResponse respondToGroupInvitation(RespondToInvitationRequest request);
}
