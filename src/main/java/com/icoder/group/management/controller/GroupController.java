package com.icoder.group.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.*;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> createGroup(@Valid @RequestBody CreateGroupRequest groupDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(groupDetails));
    }

    @PutMapping("/join")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> joinGroup(@RequestBody GroupIdRequest groupIdRequest){
        return ResponseEntity.ok(groupService.joinGroup(groupIdRequest));
    }
    @PutMapping("/members/add")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> addMemberToGroup(@RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.addMemberToGroup(groupMemberActionRequest));
    }

    @PutMapping("members/promote")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> promoteMemberToManager(
            @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.promoteMemberToManager(groupMemberActionRequest));
    }

    @PutMapping("/members/demote")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> demoteManagerToMember(
            @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.demoteManagerToMember(groupMemberActionRequest));
    }
    @PutMapping()
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> updateGroupDetails(
            @Valid @RequestBody UpdateGroupRequest updateGroupRequest){
        return ResponseEntity.ok(groupService.updateGroupDetails(updateGroupRequest));
    }

    @DeleteMapping("/members/remove")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> removeMemberFromGroup(@RequestBody GroupMemberActionRequest group){
        return ResponseEntity.ok(groupService.removeMemberFromGroup(group));
    }
}
