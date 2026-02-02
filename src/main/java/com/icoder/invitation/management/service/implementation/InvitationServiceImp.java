package com.icoder.invitation.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.util.GroupUtil;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.enums.InvitationResponse;
import com.icoder.invitation.management.enums.InvitationStatus;
import com.icoder.invitation.management.repository.InvitationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InvitationServiceImp implements InvitationService{
    private final InvitationRepository invitationRepository;
    private final GroupUtil groupUtil;


    @Override
    @Transactional
    public MessageResponse respondToGroupInvitation(RespondToInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid or expired invitation token"));
        Group group = groupUtil.findGroupById(invitation.getTargetId());
        if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
            throw new ApiException("This invitation has already been used");
        }

        if (invitation.getExpiryDate().isBefore(Instant.now())) {
            throw new ApiException("This invitation has expired");
        }
        if(request.getResponse().equals(InvitationResponse.ACCEPTED)){
            groupUtil.addUserToGroup(invitation.getRecipient(), group);

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            return new MessageResponse("Welcome to " + group.getName() + "!");
        }
        invitation.setStatus(InvitationStatus.REJECTED);
        return new MessageResponse("Invitation rejected");
    }
}
