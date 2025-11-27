package com.icoder.group.management.service.implementation;

import com.icoder.core.dto.MessageResponse;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.service.interfaces.GroupService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    public MessageResponse createGroup(CreateGroupRequest groupDetails, Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User owner = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Group group = groupMapper.toEntity(groupDetails);

        group.setOwner(owner);
        group.setCreatedAt(Instant.now());

        do{
            group.setCode(UUID.randomUUID().toString().substring(0,8).toUpperCase());
        }while (groupRepository.existsByCode(group.getCode()));
        groupRepository.save(group);
        return new MessageResponse("Group created successfully");
    }
}
