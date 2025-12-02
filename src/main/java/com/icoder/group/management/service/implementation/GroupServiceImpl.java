package com.icoder.group.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.service.interfaces.GroupService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

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
        User owner = userRepository.findById(authenticationService.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Group group = groupMapper.toEntity(groupDetails);

        group.setOwner(owner);
        group.setCreatedAt(Instant.now());

        do {
            group.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } while (groupRepository.existsByCode(group.getCode()));
        groupRepository.save(group);
        return new MessageResponse("Group created successfully");
    }

    @Override
    @Transactional
    public MessageResponse joinGroup(Long groupId) {
        return groupRepository.addUserToGroup(authenticationService.getCurrentUserId(), groupId, "MEMBER");
    }
}