package com.icoder.group.management.util;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class GroupUtil {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final AuthenticationService authenticationService;

    public User findCurrentUser() {
        return userRepository.findById(authenticationService.getCurrentUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public User findUser(String userHandle) {
        return userRepository.findByHandle(userHandle)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
    }

    public Group findGroupByCode(String code){
        return groupRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
    }

    public UserGroupRole findUserRole(User member, Group group) {
        return userGroupRoleRepository.findByUserAndGroup(member, group)
                .orElseThrow(() -> new NoSuchElementException("User is not a member of this group"));
    }

    public void checkLeaderPermission(Group group) {
        Long currentUserId = authenticationService.getCurrentUserId();
        boolean isLeader = userGroupRoleRepository.isLeaderOfGroup(currentUserId, group.getId());

        if (!isLeader) {
            throw new AccessDeniedException("Only group leaders (OWNER/MANAGER) can perform this action.");
        }
    }

    public void addUserToGroup(User user, Group group) {
        if (userGroupRoleRepository.existInGroup(user.getId(), group.getId())) {
            throw new IllegalArgumentException("User is already a member of the group");
        }

        UserGroupRole userRole = new UserGroupRole();
        userRole.setUser(user);
        userRole.setGroup(group);
        userRole.setRole(GroupRole.MEMBER);

        group.getUserRoles().add(userRole);
        // if didn't work try saving group instead
        userGroupRoleRepository.save(userRole);
    }

    public boolean updateField(String fieldValue, Consumer<String> setter) {
        if (fieldValue != null) {
            if (fieldValue.isBlank()) throw new IllegalArgumentException("Field cannot be blank");
            setter.accept(fieldValue);
            return true;
        }
        return false;
    }
}
