package com.icoder.group.management.service.implementation;

import com.cloudinary.Cloudinary;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.ImageService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.mapper.UserGroupRoleMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.group.management.service.interfaces.GroupService;
import com.icoder.group.management.util.GroupUtil;
import com.icoder.group.management.dto.GroupMemberResponse;
import com.icoder.core.dto.PictureUrlResponse;
import com.icoder.user.management.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final SecurityUtils securityUtils;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final GroupUtil groupUtil;
    private final UserGroupRoleMapper userGroupRoleMapper;
    private final Cloudinary cloudinary;
    private final ImageService imageService;
    @Override
    public Page<GroupResponse> getMyGroups(Pageable pageable) {
        Page<Group> myGroups = groupRepository.getMyGroups(securityUtils.getCurrentUserUsername(), pageable);
        return myGroups.map(groupMapper::toDTO);
    }

    @Override
    public Page<GroupResponse> getAllGroups(Pageable pageable) {
        Page<Group> groups = groupRepository.getAllPublicGroups(Visibility.PUBLIC, pageable);
        return groups.map(groupMapper::toDTO);
    }

    @Override
    public Page<GroupMemberResponse> getAllMembers(Long groupId, Pageable pageable) {
        Page<UserGroupRole> userRoles = userGroupRoleRepository.
                findAllByGroupId(groupId, pageable);
        return userRoles.map(userGroupRoleMapper::toMemberDTO);
    }

    @Override
    public ResponseEntity<Page<GroupResponse>> searchByGroupName(String query, Pageable pageable) {
        Page<Group> groups = groupRepository.findByNameContainingIgnoreCaseAndVisibility(query, Visibility.PUBLIC, pageable);
        return ResponseEntity.ok(groups.map(groupMapper::toDTO));
    }

    @Override
    public Long getMembersCount(Long groupId) {
        return userGroupRoleRepository.countByGroupId(groupId);
    }

    @Override
    @Transactional
    public MessageResponse createGroup(CreateGroupRequest groupDetails) {
        Group group = groupMapper.toEntity(groupDetails);
        group.setCreatedAt(Instant.now());

        group.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        User owner = groupUtil.findCurrentUser();

        UserGroupRole ownerRole = new UserGroupRole();
        ownerRole.setUser(owner);
        ownerRole.setGroup(group);
        ownerRole.setRole(GroupRole.OWNER);

        group.getUserRoles().add(ownerRole);
        groupRepository.save(group);
        return new MessageResponse("Group created successfully");
    }

    @Override
    @Transactional
    public MessageResponse joinPublicGroup(Long groupId) {
        User user = groupUtil.findCurrentUser();

        Group group = groupUtil.findGroupById(groupId);

        if (group.getVisibility() == Visibility.PRIVATE) {
            throw new AccessDeniedException("Cannot join a private group without an invitation");
        }

        groupUtil.addUserToGroup(user, group);

        return new MessageResponse("Joined group successfully");
    }

    @Override
    public MessageResponse joinGroupByCode(String groupCode) {
        Group group = groupUtil.findGroupByCode(groupCode);
        if (!group.getCodeEnabled()) {
            throw new AccessDeniedException("Group code-based joining is disabled");
        }
        User user = groupUtil.findCurrentUser();

        groupUtil.addUserToGroup(user, group);

        return new MessageResponse("Joined group successfully");
    }

    @Override
    @Transactional
    public MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest) {

        User newMember = groupUtil.findUser(groupMemberActionRequest.getUserHandle());

        Group group = groupUtil.findGroupById(groupMemberActionRequest.getGroupId());

        if(group.getVisibility() == Visibility.PRIVATE) {
            groupUtil.checkLeaderPermission(group);
        }

        groupUtil.addUserToGroup(newMember, group);
        return new MessageResponse("User added to group successfully");
    }

    @Override
    @Transactional
    public MessageResponse promoteMemberToManager(GroupMemberActionRequest groupMemberActionRequest) {
        User member = groupUtil.findUser(groupMemberActionRequest.getUserHandle());

        Group group = groupUtil.findGroupById(groupMemberActionRequest.getGroupId());

        groupUtil.checkLeaderPermission(group);

        UserGroupRole userRole = groupUtil.findUserRole(member, group);

        if (userRole.getRole() == GroupRole.MANAGER || userRole.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("User is already a manager or owner");
        }

        userRole.setRole(GroupRole.MANAGER);
        userGroupRoleRepository.save(userRole);
        return new MessageResponse("User promoted to manager successfully");
    }

    @Override
    @Transactional
    public MessageResponse demoteManagerToMember(GroupMemberActionRequest groupMemberActionRequest) {
        User member = groupUtil.findUser(groupMemberActionRequest.getUserHandle());

        Group group = groupUtil.findGroupById(groupMemberActionRequest.getGroupId());

        groupUtil.checkLeaderPermission(group);

        UserGroupRole userRole = groupUtil.findUserRole(member, group);

        if (userRole.getRole() == GroupRole.MEMBER) {
            throw new IllegalArgumentException("The user is already a member");
        }else if (userRole.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("Cannot demote the group owner");
        }

        userRole.setRole(GroupRole.MEMBER);
        userGroupRoleRepository.save(userRole);
        return new MessageResponse("User demoted to member successfully");
    }

    @Override
    @Transactional
    public MessageResponse removeMemberFromGroup(Long groupId, String userHandle) {
        User member = groupUtil.findUser(userHandle);

        Group group = groupUtil.findGroupById(groupId);

        groupUtil.checkLeaderPermission(group);

        UserGroupRole userRole = groupUtil.findUserRole(member, group);
        if(userRole.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the group owner");
        }

        userGroupRoleRepository.delete(userRole);
        return new MessageResponse("User removed from group successfully");
    }

    @Override
    @Transactional
    public MessageResponse updateGroupDetails(UpdateGroupRequest updateGroupRequest) {

        Group group = groupUtil.findGroupById(updateGroupRequest.getGroupId());
        groupUtil.checkLeaderPermission(group);

        boolean updated = false;

        updated |= groupUtil.updateField(updateGroupRequest.getName(), group::setName);
        updated |= groupUtil.updateField(updateGroupRequest.getDescription(), group::setDescription);

        if(updateGroupRequest.getVisibility() != null) {
            group.setVisibility(updateGroupRequest.getVisibility());
            updated = true;
        }
        if(updateGroupRequest.getContestCoordinatorType() != null) {
            group.setContestCoordinatorType(updateGroupRequest.getContestCoordinatorType());
            updated = true;
        }
        if (!updated) {
            throw new IllegalArgumentException("No valid fields provided for update");
        }
        groupRepository.save(group);
        return new MessageResponse("Group details updated successfully");

    }

    @Override
    @Transactional
    public MessageResponse updateGroupPicture(UpdateGroupPictureRequest pictureRequest) {
        String folderPath = "groups/profile-pictures";
        Group group = groupUtil.findGroupById(pictureRequest.getGroupId());

        groupUtil.checkLeaderPermission(group);

        imageService.checkPictureType(pictureRequest.getPicture());

        try {
            if (group.getPictureUrl() != null) {
                imageService.deleteImageFromCloudinary(group.getPictureUrl(), folderPath);
            }
            Map<String, Object> uploadResult = cloudinary.uploader().upload(pictureRequest.getPicture().getBytes(),
                    Map.of("folder", folderPath));

            String imageUrl = uploadResult.get("secure_url").toString();
            group.setPictureUrl(imageUrl);
            groupRepository.save(group);

            return new MessageResponse("Group picture updated successfully");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload group picture", e);
        }
    }

    @Override
    public PictureUrlResponse viewGroupPicture(Long groupId) {
        Group group = groupUtil.findGroupById(groupId);
        return PictureUrlResponse.builder()
                .pictureUrl(group.getPictureUrl())
                .build();
    }
    @Override
    @Transactional
    public MessageResponse deleteGroupPicture(Long groupId) {
        String folderPath = "groups/profile-pictures";
        Group group = groupUtil.findGroupById(groupId);

        groupUtil.checkLeaderPermission(group);

        String pictureUrl = group.getPictureUrl();

        if (pictureUrl == null || pictureUrl.isBlank()) {
            throw new ApiException("Group does not have a profile picture");
        }

        group.setPictureUrl(null);
        groupRepository.save(group);

        try {
            imageService.deleteImageFromCloudinary(pictureUrl, folderPath);
        } catch (Exception e) {
            log.warn("Failed to delete profile image from Cloudinary", e);
        }

        return new MessageResponse("Your profile picture has been successfully deleted");
    }
}