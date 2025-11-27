package com.icoder.group.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.CreateGroupRequest;
import org.springframework.security.core.Authentication;

public interface GroupService {
    MessageResponse createGroup(CreateGroupRequest groupDetails, Authentication authentication);
}
