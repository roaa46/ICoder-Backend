package com.icoder.group.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.dto.GroupMemberResponse;
import com.icoder.core.dto.PictureUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface GroupService {
    Page<GroupResponse> getMyGroups(Pageable pageable);
    Page<GroupResponse> getAllGroups(Pageable pageable);
    Page<GroupMemberResponse> getAllMembers(Long groupId, Pageable pageable);
    ResponseEntity<Page<GroupResponse>> searchByGroupName(String query, Pageable pageable);
    Long getMembersCount(Long groupId);
    MessageResponse createGroup(CreateGroupRequest groupDetails);
    MessageResponse joinPublicGroup(Long groupId);
    MessageResponse joinGroupByCode(String groupCode);
    MessageResponse addMemberToGroup(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse promoteMemberToManager(GroupMemberActionRequest groupMemberActionRequest);
    MessageResponse demoteManagerToMember(GroupMemberActionRequest groupMemberActionRequest);

    MessageResponse removeMemberFromGroup(Long groupId, String userHandle);

    MessageResponse updateGroupDetails(UpdateGroupRequest updateGroupRequest);
    MessageResponse updateGroupPicture(UpdateGroupPictureRequest updateGroupPictureRequest);
    PictureUrlResponse viewGroupPicture(Long groupId);
    MessageResponse deleteGroupPicture(Long groupId);
}
