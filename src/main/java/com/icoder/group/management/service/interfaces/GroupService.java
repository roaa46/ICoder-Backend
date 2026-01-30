package com.icoder.group.management.service.interfaces;

import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupService {
    Page <GroupResponse> GetMyGroups(Pageable pageable);
    Page<GroupResponse> getAllGroups(Pageable pageable);

    MessageResponse createGroup(CreateGroupRequest groupDetails);
    MessageResponse joinGroup(GroupIdRequest groupDetails);
    MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse promoteMemberToManager(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse demoteManagerToMember(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse removeMemberFromGroup(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse updateGroupDetails(UpdateGroupRequest updateGroupRequest);

    Set<ManagedGroupsResponse> getManagedGroups();

    Page<GroupContestsResponse> viewContestsInGroup(Long groupId, Pageable pageable);
}
