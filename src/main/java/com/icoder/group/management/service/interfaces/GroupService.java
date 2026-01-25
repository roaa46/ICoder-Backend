package com.icoder.group.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.dto.GroupMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {
    Page <GroupResponse> GetMyGroups(Pageable pageable);
    Page<GroupResponse> getAllGroups(Pageable pageable);
    Page<GroupMemberResponse> getAllMembers(GroupIdRequest groupIdRequest, Pageable pageable);

    MessageResponse createGroup(CreateGroupRequest groupDetails);
    MessageResponse joinGroup(GroupIdRequest groupDetails);
    MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse promoteMemberToManager(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse demoteManagerToMember(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse removeMemberFromGroup(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse updateGroupDetails(UpdateGroupRequest updateGroupRequest);
}
