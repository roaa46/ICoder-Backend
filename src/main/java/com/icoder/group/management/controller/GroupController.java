package com.icoder.group.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.dto.PictureUrlResponse;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.service.implementation.GroupServiceImpl;
import com.icoder.group.management.dto.GroupMemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @GetMapping("/{groupId}/members")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupMemberResponse>> getAllMembers(
            @PathVariable Long groupId,
            Pageable pageable){
            return ResponseEntity.ok(groupService.getAllMembers(groupId, pageable));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> createGroup(@Valid @RequestBody CreateGroupRequest groupDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(groupDetails));
    }

    @PutMapping("/{groupId}/join")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> joinGroup(@PathVariable Long groupId){
        return ResponseEntity.ok(groupService.joinGroup(groupId));
    }
    @PutMapping("/members/add")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> addMemberToGroup(
            @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.addMemberToGroup(groupMemberActionRequest));
    }

    @PutMapping("/members/promote")
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
    @PutMapping("")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> updateGroupDetails(
            @Valid @RequestBody UpdateGroupRequest updateGroupRequest){
        return ResponseEntity.ok(groupService.updateGroupDetails(updateGroupRequest));
    }
    @DeleteMapping("/{groupId}/members")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> removeMemberFromGroup(Long groupId, @RequestParam String userHandle){
        return ResponseEntity.ok(groupService.removeMemberFromGroup(groupId, userHandle));
    }

    @PutMapping(value = "/group-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> updateGroupPicture(
            @Valid @ModelAttribute UpdateGroupPictureRequest updateGroupPictureRequest) {

        return ResponseEntity.ok(groupService.updateGroupPicture(updateGroupPictureRequest));
    }
    @GetMapping("/{groupId}/group-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PictureUrlResponse> viewGroupPicture(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.viewGroupPicture(groupId));
    }

    @DeleteMapping("/{groupId}/group-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteGroupPicture(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.deleteGroupPicture(groupId));
    }
}
