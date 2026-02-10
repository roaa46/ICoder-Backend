package com.icoder.group.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.dto.PictureUrlResponse;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.service.implementation.GroupServiceImpl;
import com.icoder.group.management.dto.GroupMemberResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "Get group by ID",
            description = "Retrieves a group by its unique ID."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @Operation(
            summary = "Get my groups",
            description = "Retrieves all groups that the authenticated user is a member of with pagination support."
    )
    @GetMapping("/me")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupResponse>> getMyGroups(
           @PageableDefault(size = 9, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(groupService.getMyGroups(pageable));
    }
    @Operation(
            summary = "Get all public groups",
            description = "Retrieves all publicly visible groups with pagination support."
    )
    @GetMapping("")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupResponse>> getAllGroups(
            @PageableDefault(size = 9, sort = "name") Pageable pageable){
        return ResponseEntity.ok(groupService.getAllGroups(pageable));
    }
    @Operation(
            summary = "Get all group members",
            description = "Retrieves all members of a specific group including their roles with pagination support."
    )
    @GetMapping("/{groupId}/members")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupMemberResponse>> getAllMembers(
            @PathVariable Long groupId,
            Pageable pageable){
            return ResponseEntity.ok(groupService.getAllMembers(groupId, pageable));
    }
    @Operation(
            summary = "Search groups by name",
            description = "Retrieves groups that match the search query with pagination support."
    )
    @GetMapping(params = "query")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Page<GroupResponse>> searchPublicGroupsByName(
            @RequestParam("query") String query, @PageableDefault(size = 9, sort = "name") Pageable pageable){
        return ResponseEntity.ok(groupService.searchByGroupName(query, pageable));
    }

    @Operation(
            summary = "Create a new group",
            description = "Creates a new group with the authenticated user as the owner. Generates a unique group code automatically."
    )
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> createGroup(@Valid @RequestBody CreateGroupRequest groupDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(groupDetails));
    }

    @Operation(
            summary = "Join a public group",
            description = "Allows the authenticated user to join a public group by providing its ID."
    )
    @PutMapping("/{groupId}/join")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> joinPublicGroup(@PathVariable Long groupId){
        return ResponseEntity.ok(groupService.joinPublicGroup(groupId));
    }

    @Operation(
            summary = "Join group by code",
            description = "Allows the authenticated user to join a group using an 8-character invite code."
    )
    @PutMapping("join")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> joinGroupByCode(@Valid @RequestParam String code){
        return ResponseEntity.ok(groupService.joinGroupByCode(code));
    }

    @Operation(
            summary = "Add member to group",
            description = "Allows group leader or manager to add a new member to the group by their user handle."
    )
    @PutMapping("/members/add")
    @PreAuthorize("@groupUtil.hasManagerPermission(#groupMemberActionRequest.groupId)")
    public ResponseEntity<MessageResponse> addMemberToGroup(
            @Valid @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.addMemberToGroup(groupMemberActionRequest));
    }

    @Operation(
            summary = "Promote member to manager",
            description = "Allows group leader to promote a regular member to manager role with additional permissions."
    )
    @PutMapping("/members/promote")
    @PreAuthorize("@groupUtil.hasOwnerPermission(#groupMemberActionRequest.groupId)")
    public ResponseEntity<MessageResponse> promoteMemberToManager(
            @Valid @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.promoteMemberToManager(groupMemberActionRequest));
    }

    @Operation(
            summary = "Demote manager to member",
            description = "Allows group leader to demote a manager back to regular member role."
    )
    @PutMapping("/members/demote")
    @PreAuthorize("@groupUtil.hasOwnerPermission(#groupMemberActionRequest.groupId)")
        public ResponseEntity<MessageResponse> demoteManagerToMember(
            @Valid @RequestBody GroupMemberActionRequest groupMemberActionRequest){
        return ResponseEntity.ok(groupService.demoteManagerToMember(groupMemberActionRequest));
    }
    @Operation(
            summary = "Update group details",
            description = "Allows group leader to update group information such as name, description, visibility, and contest coordinator type."
    )
    @PutMapping("")
    @PreAuthorize("@groupUtil.hasManagerPermission(#updateGroupRequest.groupId)")
    public ResponseEntity<MessageResponse> updateGroupDetails(
            @Valid @RequestBody UpdateGroupRequest updateGroupRequest){
        return ResponseEntity.ok(groupService.updateGroupDetails(updateGroupRequest));
    }
    @Operation(
            summary = "Remove member from group",
            description = "Allows group leader to remove a member from the group. The group owner cannot be removed."
    )
    @DeleteMapping("/{groupId}/members")
    @PreAuthorize("@groupUtil.hasManagerPermission(#groupId)")
    public ResponseEntity<MessageResponse> removeMemberFromGroup(
            @PathVariable Long groupId, @RequestParam(value = "handle") String userHandle){
        return ResponseEntity.ok(groupService.removeMemberFromGroup(groupId, userHandle));
    }

    @Operation(
            summary = "Update group picture",
            description = "Allows group leader to upload or update the group's profile picture."
    )
    @PutMapping(value = "/{groupId}/group-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@groupUtil.hasManagerPermission(#updateGroupPictureRequest.groupId)")
    public ResponseEntity<MessageResponse> updateGroupPicture(
            @Valid @ModelAttribute UpdateGroupPictureRequest updateGroupPictureRequest) {

        return ResponseEntity.ok(groupService.updateGroupPicture(updateGroupPictureRequest));
    }

    @Operation(
            summary = "View group picture",
            description = "Retrieves the URL of the group's profile picture."
    )
    @GetMapping("/{groupId}/group-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PictureUrlResponse> viewGroupPicture(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.viewGroupPicture(groupId));
    }

    @Operation(
            summary = "Delete group picture",
            description = "Allows group leader to remove the group's profile picture."
    )
    @DeleteMapping("/{groupId}/group-picture")
    @PreAuthorize("@groupUtil.hasManagerPermission(#groupId)")
    public ResponseEntity<MessageResponse> deleteGroupPicture(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.deleteGroupPicture(groupId));
    }
}
