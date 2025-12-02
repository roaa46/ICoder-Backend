package com.icoder.group.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface GroupService {
    Page <GroupResponse> GetMyGroups(Authentication authentication, Pageable pageable);
    Page<GroupResponse> getAllGroups(Pageable pageable);

    MessageResponse createGroup(CreateGroupRequest groupDetails, Authentication authentication);

    void joinGroup(Long groupId, Authentication authentication);
}
