package com.icoder.invitation.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.user.management.entity.User;

public interface InvitationService {
    Invitation sendGroupInvitation(Long groupId, User sender, User recipient);
    MessageResponse respondToGroupInvitation(RespondToInvitationRequest request);
}
