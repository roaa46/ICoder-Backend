package com.icoder.invitation.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.service.interfaces.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invite")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PutMapping("/group-response")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> respondToGroupInvitation(
            @Valid @RequestBody RespondToInvitationRequest request){
        return ResponseEntity.ok(invitationService.respondToGroupInvitation(request));
    }
}
