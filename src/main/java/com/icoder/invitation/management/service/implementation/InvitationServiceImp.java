package com.icoder.invitation.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.util.GroupUtil;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.enums.InvitationResponse;
import com.icoder.invitation.management.enums.InvitationStatus;
import com.icoder.invitation.management.enums.InvitationType;
import com.icoder.invitation.management.repository.InvitationRepository;
import com.icoder.invitation.management.service.interfaces.InvitationService;
import com.icoder.invitation.management.utils.InvitationUtils;
import com.icoder.user.management.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImp implements InvitationService {
    private final InvitationRepository invitationRepository;
    private final GroupUtil groupUtil;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public Invitation sendGroupInvitation(Long groupId, User sender, User recipient) {
        boolean hasPendingInvitation = invitationRepository.existsPendingInvitation(
                groupId,
                InvitationType.GROUP,
                recipient,
                InvitationStatus.PENDING,
                Instant.now()
        );

        if (hasPendingInvitation) {
            log.warn("Attempted to send duplicate invitation for group {} to user {}", groupId, recipient.getId());
            throw new ApiException("User already has a pending invitation for this group");
        }

        Invitation invitation = InvitationUtils.groupInvitationBuilder(groupId, sender, recipient);
        Invitation savedInvitation = invitationRepository.save(invitation);
        log.info("Group invitation sent: groupId={}, senderId={}, recipientId={}, token={}",
                groupId, sender.getId(), recipient.getId(), savedInvitation.getToken());

        return savedInvitation;
    }

    @Override
    @Transactional
    public MessageResponse respondToGroupInvitation(RespondToInvitationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid or expired invitation token"));

        // Verify the current user is the invitation recipient
        Long currentUserId = securityUtils.getCurrentUserId();
        if (!invitation.getRecipient().getId().equals(currentUserId)) {
            log.warn("User {} attempted to respond to invitation meant for user {}",
                    currentUserId, invitation.getRecipient().getId());
            throw new ApiException("You are not authorized to respond to this invitation");
        }

        // Check if invitation is still pending
        if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
            log.info("Attempted to use already processed invitation: token={}, status={}",
                    request.getToken(), invitation.getStatus());
            throw new ApiException("This invitation has already been " +
                    invitation.getStatus().toString().toLowerCase());
        }

        // Check if invitation is expired
        if (invitation.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Attempted to use expired invitation: token={}, expiryDate={}",
                    request.getToken(), invitation.getExpiryDate());
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);
            throw new ApiException("This invitation has expired");
        }

        Group group = groupUtil.findGroupById(invitation.getTargetId());

        // Handle the response
        if (request.getResponse().equals(InvitationResponse.ACCEPTED)) {
            groupUtil.addUserToGroup(invitation.getRecipient(), group);
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            log.info("Invitation accepted: token={}, userId={}, groupId={}",
                    request.getToken(), currentUserId, group.getId());
            return MessageResponse.builder()
                    .message("Welcome to " + group.getName() + "!")
                    .build();
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);

            log.info("Invitation rejected: token={}, userId={}, groupId={}",
                    request.getToken(), currentUserId, group.getId());
            return MessageResponse.builder()
                    .message("Invitation rejected")
                    .build();
        }
    }
}
