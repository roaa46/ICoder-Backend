package com.icoder.invitation.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.invitation.management.dto.RespondToInvitationRequest;
import com.icoder.invitation.management.service.interfaces.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invite")
@Tag(name = "Invitation Management")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PutMapping("/group-response")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Respond to a group invitation", description = "Respond to a group invitation")
    public ResponseEntity<MessageResponse> respondToGroupInvitation(
            @Valid @RequestBody RespondToInvitationRequest request) {
        return ResponseEntity.ok(invitationService.respondToGroupInvitation(request));
    }
}
