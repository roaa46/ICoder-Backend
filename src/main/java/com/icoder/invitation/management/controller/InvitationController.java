package com.icoder.invitation.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.service.implementation.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/invite")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PutMapping("group-response")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> respondToGroupInvitation(
            @RequestBody RespondToInvitationRequest request){
        return ResponseEntity.ok(invitationService.respondToGroupInvitation(request));
    }
}
