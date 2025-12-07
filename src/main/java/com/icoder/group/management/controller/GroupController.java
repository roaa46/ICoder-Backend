package com.icoder.group.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.GroupMemberActionRequest;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import com.icoder.group.management.dto.JoinGroupRequest;
import com.icoder.group.management.service.implementation.GroupServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupServiceImpl groupService;

    @GetMapping("/me")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupResponse>> getMyGroups(
           @PageableDefault(size = 9, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(groupService.GetMyGroups(pageable));
    }
    @GetMapping("")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupResponse>> getAllGroups(
            @PageableDefault(size = 9, sort = "name") Pageable pageable){
        return ResponseEntity.ok(groupService.getAllGroups(pageable));
    }

    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createGroup(@Valid @RequestBody CreateGroupRequest groupDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(groupDetails));
    }

    @PutMapping("/join")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> joinGroup(@RequestBody JoinGroupRequest joinGroupRequest){
        return ResponseEntity.ok(groupService.joinGroup(joinGroupRequest));
    }
    @PutMapping("/members/add")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> addMemberToGroup(@RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.addMemberToGroup(groupMemberActionRequest));
    }
}
