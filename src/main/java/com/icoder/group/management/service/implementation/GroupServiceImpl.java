package com.icoder.group.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.group.management.service.interfaces.GroupService;
import com.icoder.group.management.util.GroupUtil;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final AuthenticationService authenticationService;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final GroupUtil groupUtil;
    private final SecurityUtils securityUtils;

    @Override
    public Page<GroupResponse> GetMyGroups(Pageable pageable) {
        Page<Group> myGroups = groupRepository.getMyGroups(authenticationService.getCurrentUserUsername(), pageable);
        return myGroups.map(groupMapper::toDTO);
    }

    @Override
    public Page<GroupResponse> getAllGroups(Pageable pageable) {
        Page<Group> groups = groupRepository.getAllPublicGroups(Visibility.PUBLIC, pageable);
        return groups.map(groupMapper::toDTO);
    }

    @Override
    @Transactional
    public MessageResponse createGroup(CreateGroupRequest groupDetails) {
        Group group = groupMapper.toEntity(groupDetails);
        group.setCreatedAt(Instant.now());
        group.setContestCoordinatorType(groupDetails.getContestCoordinatorType());
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
    public MessageResponse joinGroup(GroupIdRequest groupIdRequest) {
        User user = groupUtil.findCurrentUser();

        Group group = groupUtil.findGroup(groupIdRequest.getGroupId());

        if (group.getVisibility() == Visibility.PRIVATE) {
            throw new AccessDeniedException("Cannot join a private group without an invitation");
        }

        groupUtil.addUserToGroup(user, group);

        return new MessageResponse("Joined group successfully");
    }

    @Override
    @Transactional
    public MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest) {

        User newMember = groupUtil.findUser(groupMemberActionRequest.getUserHandle());

        Group group = groupUtil.findGroup(groupMemberActionRequest.getGroupId());

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

        Group group = groupUtil.findGroup(groupMemberActionRequest.getGroupId());

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

        Group group = groupUtil.findGroup(groupMemberActionRequest.getGroupId());

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
    public MessageResponse removeMemberFromGroup(GroupMemberActionRequest groupMemberActionRequest) {
        User member = groupUtil.findUser(groupMemberActionRequest.getUserHandle());

        Group group = groupUtil.findGroup(groupMemberActionRequest.getGroupId());

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

        Group group = groupUtil.findGroup(updateGroupRequest.getGroupId());
        groupUtil.checkLeaderPermission(group);

        boolean updated = false;

        updated |= groupUtil.updateField(updateGroupRequest.getName(), group::setName);
        updated |= groupUtil.updateField(updateGroupRequest.getDescription(), group::setDescription);
        updated |= groupUtil.updateField(updateGroupRequest.getPictureUrl(), group::setPictureUrl);

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
    public Set<ManagedGroupsResponse> getManagedGroups() {
        Long userId = securityUtils.getCurrentUserId();

        Set<Group> groups = groupRepository.findManagedGroupsByUserId(userId);

        log.info("User {} has {} managed groups", userId, groups.size());

        return groups.stream()
                .map(group -> new ManagedGroupsResponse(
                        String.valueOf(group.getId()),
                        group.getName()
                ))
                .collect(Collectors.toSet());
    }
}