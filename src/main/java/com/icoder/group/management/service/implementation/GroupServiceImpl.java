package com.icoder.group.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.GroupMemberActionRequest;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import com.icoder.group.management.dto.JoinGroupRequest;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.group.management.service.interfaces.GroupService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserGroupRoleRepository userGroupRoleRepository;

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
    public MessageResponse createGroup(CreateGroupRequest groupDetails) {
        Group group = groupMapper.toEntity(groupDetails);
        group.setCreatedAt(Instant.now());
        do {
            group.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } while (groupRepository.existsByCode(group.getCode()));

        User owner = userRepository.findById(authenticationService.getCurrentUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
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
    public MessageResponse joinGroup(JoinGroupRequest joinGroupRequest) {
        User user = userRepository.findById(authenticationService.getCurrentUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Group group = groupRepository.findById(joinGroupRequest.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        if (group.getVisibility() == Visibility.PRIVATE) {
            throw new AccessDeniedException("Cannot join a private group without an invitation");
        }

        addUserToGroup(user, group);

        return new MessageResponse("Joined group successfully");
    }

    @Override
    @Transactional
    public MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest) {

        User newMember = userRepository.findByHandle(groupMemberActionRequest.getUserHandle())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Group group = groupRepository.findById(groupMemberActionRequest.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        Set<User> leaders = groupRepository.getLeaders(group.getId());
        if (group.getVisibility() == Visibility.PRIVATE &&
                leaders.stream().noneMatch(user -> user.getId().equals(authenticationService.getCurrentUserId()))) {
            throw new AccessDeniedException("Only group leaders can add members");
        }

        addUserToGroup(newMember, group);

        return new MessageResponse("User added to group successfully");
    }

    @Override
    @Transactional
    public MessageResponse removeMemberFromGroup(GroupMemberActionRequest groupMemberActionRequest) {
        User member = userRepository.findByHandle(groupMemberActionRequest.getUserHandle())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Group group = groupRepository.findById(groupMemberActionRequest.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        Set<User> leaders = groupRepository.getLeaders(group.getId());
        if (leaders.stream().noneMatch(user -> user.getId().equals(authenticationService.getCurrentUserId()))) {
            throw new AccessDeniedException("Only group leaders can remove members");
        }

        if (!groupRepository.existInGroup(member.getId(), group.getId())) {
            throw new IllegalArgumentException("User is not a member of the group");
        }


        UserGroupRole userRole = userGroupRoleRepository.findByUserAndGroup(member, group)
                .orElseThrow(() -> new NoSuchElementException("User role not found"));
        userGroupRoleRepository.delete(userRole);
        return new MessageResponse("User removed from group successfully");
    }

    // utility method to add user to group
    private void addUserToGroup(User user, Group group) {
        if (groupRepository.existInGroup(user.getId(), group.getId())) {
            throw new IllegalArgumentException("User is already a member of the group");
        }

        UserGroupRole userRole = new UserGroupRole();
        userRole.setUser(user);
        userRole.setGroup(group);
        userRole.setRole(GroupRole.MEMBER);

        group.getUserRoles().add(userRole);
        groupRepository.save(group);
    }
}