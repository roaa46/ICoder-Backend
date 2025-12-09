package com.icoder.group.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.GroupMemberActionRequest;
import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import com.icoder.group.management.dto.JoinGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    Page <GroupResponse> GetMyGroups(Pageable pageable);
    Page<GroupResponse> getAllGroups(Pageable pageable);

    MessageResponse createGroup(CreateGroupRequest groupDetails);
    MessageResponse joinGroup(JoinGroupRequest groupDetails);
    MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse promoteMemberToManager(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse demoteManagerToMember(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse removeMemberFromGroup(GroupMemberActionRequest groupMemberActionRequest);

}
