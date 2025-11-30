package com.icoder.group.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.service.implementation.GroupServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupServiceImpl groupService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> createGroup(@Valid @RequestBody CreateGroupRequest groupDetails, Authentication authentication) {
        return ResponseEntity.ok(groupService.createGroup(groupDetails, authentication));
    }
}
