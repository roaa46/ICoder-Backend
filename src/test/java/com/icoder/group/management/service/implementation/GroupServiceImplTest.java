package com.icoder.group.management.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.dto.PictureUrlResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.ImageService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.dto.*;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.mapper.GroupMapper;
import com.icoder.group.management.mapper.UserGroupRoleMapper;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.group.management.util.GroupUtil;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.service.interfaces.InvitationService;
import com.icoder.notification.management.events.InvitationSentEvent;
import com.icoder.user.management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMapper groupMapper;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserGroupRoleRepository userGroupRoleRepository;
    @Mock
    private GroupUtil groupUtil;
    @Mock
    private UserGroupRoleMapper userGroupRoleMapper;
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private ImageService imageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private InvitationService invitationService;
    @Mock
    private ContestRepository contestRepository;
    @Mock
    private ContestMapper contestMapper;
    @Mock
    private Uploader uploader;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group group;
    private Group privateGroup;
    private User user;
    private User memberUser;
    private UserGroupRole userGroupRole;
    private GroupResponse groupResponse;
    private GroupMemberResponse memberResponse;
    private CreateGroupRequest createGroupRequest;
    private GroupMemberActionRequest memberActionRequest;
    private UpdateGroupRequest updateGroupRequest;
    private UpdateGroupPictureRequest updateGroupPictureRequest;
    private GroupContestsResponse groupContestsResponse;
    private Pageable pageable;
    private Contest contest;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(groupService, "groupPictureFolder", "groups/pictures");

        group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setDescription("Test Description");
        group.setVisibility(Visibility.PUBLIC);
        group.setCode("ABC12345");
        group.setCodeEnabled(true);
        group.setPictureUrl("https://cdn.example.com/group-old.jpg");
        group.setUserRoles(new HashSet<>());

        privateGroup = new Group();
        privateGroup.setId(2L);
        privateGroup.setName("Private Group");
        privateGroup.setVisibility(Visibility.PRIVATE);
        privateGroup.setCode("PRIVATE01");
        privateGroup.setCodeEnabled(true);
        privateGroup.setUserRoles(new HashSet<>());

        user = new User();
        user.setId(10L);
        user.setHandle("owner");

        memberUser = new User();
        memberUser.setId(20L);
        memberUser.setHandle("member");

        userGroupRole = new UserGroupRole();
        userGroupRole.setUser(memberUser);
        userGroupRole.setGroup(group);
        userGroupRole.setRole(GroupRole.MEMBER);

        groupResponse = new GroupResponse();

        memberResponse = new GroupMemberResponse();

        createGroupRequest = new CreateGroupRequest();
        createGroupRequest.setName("New Group");
        createGroupRequest.setDescription("New Description");
        createGroupRequest.setVisibility(Visibility.PUBLIC);

        memberActionRequest = new GroupMemberActionRequest();
        memberActionRequest.setGroupId(1L);
        memberActionRequest.setUserHandle("member");

        updateGroupRequest = new UpdateGroupRequest();
        updateGroupRequest.setGroupId(1L);
        updateGroupRequest.setName("Updated Group");
        updateGroupRequest.setDescription("Updated Description");
        updateGroupRequest.setVisibility(Visibility.PRIVATE);

        updateGroupPictureRequest = new UpdateGroupPictureRequest();
        updateGroupPictureRequest.setGroupId(1L);
        updateGroupPictureRequest.setPicture(multipartFile);

        groupContestsResponse = new GroupContestsResponse();

        pageable = PageRequest.of(0, 10);

        contest = new Contest();
        contest.setId(100L);
        contest.setTitle("Contest 1");

        invitation = new Invitation();
    }

    @Nested
    @DisplayName("getGroupById()")
    class GetGroupByIdTests {
        @Test
        void shouldReturnGroupById() {
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupMapper.toDTO(group)).thenReturn(groupResponse);

            GroupResponse result = groupService.getGroupById(1L);

            assertNotNull(result);
            verify(groupUtil).findGroupById(1L);
            verify(groupMapper).toDTO(group);
        }
    }

    @Nested
    @DisplayName("getMyGroups()")
    class GetMyGroupsTests {
        @Test
        void shouldReturnMyGroups() {
            Page<Group> groupsPage = new PageImpl<>(List.of(group), pageable, 1);

            when(securityUtils.getCurrentUserUsername()).thenReturn("owner");
            when(groupRepository.getMyGroups("owner", pageable)).thenReturn(groupsPage);
            when(groupMapper.toDTO(group)).thenReturn(groupResponse);

            Page<GroupResponse> result = groupService.getMyGroups(pageable);

            assertEquals(1, result.getTotalElements());
            verify(groupRepository).getMyGroups("owner", pageable);
            verify(groupMapper).toDTO(group);
        }
    }

    @Nested
    @DisplayName("getAllGroups()")
    class GetAllGroupsTests {
        @Test
        void shouldReturnAllPublicGroups() {
            Page<Group> groupsPage = new PageImpl<>(List.of(group), pageable, 1);

            when(groupRepository.getAllPublicGroups(Visibility.PUBLIC, pageable)).thenReturn(groupsPage);
            when(groupMapper.toDTO(group)).thenReturn(groupResponse);

            Page<GroupResponse> result = groupService.getAllGroups(pageable);

            assertEquals(1, result.getTotalElements());
            verify(groupRepository).getAllPublicGroups(Visibility.PUBLIC, pageable);
            verify(groupMapper).toDTO(group);
        }
    }

    @Nested
    @DisplayName("getAllMembers()")
    class GetAllMembersTests {
        @Test
        void shouldReturnAllMembers() {
            Page<UserGroupRole> rolesPage = new PageImpl<>(List.of(userGroupRole), pageable, 1);

            when(userGroupRoleRepository.findAllByGroupId(1L, pageable)).thenReturn(rolesPage);
            when(userGroupRoleMapper.toMemberDTO(userGroupRole)).thenReturn(memberResponse);

            Page<GroupMemberResponse> result = groupService.getAllMembers(1L, pageable);

            assertEquals(1, result.getTotalElements());
            verify(userGroupRoleRepository).findAllByGroupId(1L, pageable);
            verify(userGroupRoleMapper).toMemberDTO(userGroupRole);
        }
    }

    @Nested
    @DisplayName("searchByGroupName()")
    class SearchByGroupNameTests {
        @Test
        void shouldSearchByGroupName() {
            Page<Group> groupsPage = new PageImpl<>(List.of(group), pageable, 1);

            when(groupRepository.findByNameContainingIgnoreCaseAndVisibility("test", Visibility.PUBLIC, pageable))
                    .thenReturn(groupsPage);
            when(groupMapper.toDTO(group)).thenReturn(groupResponse);

            Page<GroupResponse> result = groupService.searchByGroupName("test", pageable);

            assertEquals(1, result.getTotalElements());
            verify(groupRepository).findByNameContainingIgnoreCaseAndVisibility("test", Visibility.PUBLIC, pageable);
            verify(groupMapper).toDTO(group);
        }
    }

    @Nested
    @DisplayName("createGroup()")
    class CreateGroupTests {
        @Test
        void shouldCreateGroupSuccessfully() {
            Group newGroup = new Group();
            newGroup.setUserRoles(new HashSet<>());

            when(groupMapper.toEntity(createGroupRequest)).thenReturn(newGroup);
            when(groupUtil.findCurrentUser()).thenReturn(user);

            MessageResponse result = groupService.createGroup(createGroupRequest);

            assertEquals("Group created successfully", result.getMessage());
            assertEquals(user.getId(), newGroup.getOwnerId());
            assertNotNull(newGroup.getCreatedAt());
            assertNotNull(newGroup.getCode());
            assertEquals(8, newGroup.getCode().length());
            assertEquals(1, newGroup.getUserRoles().size());

            verify(groupRepository).save(newGroup);
        }
    }

    @Nested
    @DisplayName("joinPublicGroup()")
    class JoinPublicGroupTests {
        @Test
        void shouldJoinPublicGroupSuccessfully() {
            when(groupUtil.findCurrentUser()).thenReturn(user);
            when(groupUtil.findGroupById(1L)).thenReturn(group);

            MessageResponse result = groupService.joinPublicGroup(1L);

            assertEquals("Joined group successfully", result.getMessage());
            verify(groupUtil).addUserToGroup(user, group);
        }

        @Test
        void shouldThrowWhenJoiningPrivateGroup() {
            when(groupUtil.findCurrentUser()).thenReturn(user);
            when(groupUtil.findGroupById(2L)).thenReturn(privateGroup);

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> groupService.joinPublicGroup(2L)
            );

            assertEquals("Cannot join a private group without an invitation", ex.getMessage());
            verify(groupUtil, never()).addUserToGroup(any(), any());
        }
    }

    @Nested
    @DisplayName("joinGroupByCode()")
    class JoinGroupByCodeTests {
        @Test
        void shouldJoinByCodeSuccessfully() {
            when(groupUtil.findGroupByCode("ABC12345")).thenReturn(group);
            when(groupUtil.findCurrentUser()).thenReturn(user);

            MessageResponse result = groupService.joinGroupByCode("ABC12345");

            assertEquals("Joined group successfully", result.getMessage());
            verify(groupUtil).addUserToGroup(user, group);
        }

        @Test
        void shouldThrowWhenCodeJoiningDisabled() {
            group.setCodeEnabled(false);
            when(groupUtil.findGroupByCode("ABC12345")).thenReturn(group);

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> groupService.joinGroupByCode("ABC12345")
            );

            assertEquals("Group code-based joining is disabled", ex.getMessage());
            verify(groupUtil, never()).addUserToGroup(any(), any());
        }
    }

    @Nested
    @DisplayName("addMemberToGroup()")
    class AddMemberToGroupTests {
        @Test
        void shouldSendInvitationSuccessfully() {
            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findCurrentUser()).thenReturn(user);
            when(invitationService.sendGroupInvitation(1L, user, memberUser)).thenReturn(invitation);

            MessageResponse result = groupService.addMemberToGroup(memberActionRequest);

            assertEquals("Invitation sent successfully", result.getMessage());
            verify(invitationService).sendGroupInvitation(1L, user, memberUser);
            verify(eventPublisher).publishEvent(any(InvitationSentEvent.class));
        }
    }

    @Nested
    @DisplayName("promoteMemberToManager()")
    class PromoteMemberToManagerTests {
        @Test
        void shouldPromoteMemberToManager() {
            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            MessageResponse result = groupService.promoteMemberToManager(memberActionRequest);

            assertEquals("User promoted to manager successfully", result.getMessage());
            assertEquals(GroupRole.MANAGER, userGroupRole.getRole());
            verify(userGroupRoleRepository).save(userGroupRole);
        }

        @Test
        void shouldThrowWhenAlreadyManager() {
            userGroupRole.setRole(GroupRole.MANAGER);

            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> groupService.promoteMemberToManager(memberActionRequest)
            );

            assertEquals("User is already a manager or owner", ex.getMessage());
            verify(userGroupRoleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("demoteManagerToMember()")
    class DemoteManagerToMemberTests {
        @Test
        void shouldDemoteManagerToMember() {
            userGroupRole.setRole(GroupRole.MANAGER);

            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            MessageResponse result = groupService.demoteManagerToMember(memberActionRequest);

            assertEquals("User demoted to member successfully", result.getMessage());
            assertEquals(GroupRole.MEMBER, userGroupRole.getRole());
            verify(userGroupRoleRepository).save(userGroupRole);
        }

        @Test
        void shouldThrowWhenAlreadyMember() {
            userGroupRole.setRole(GroupRole.MEMBER);

            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> groupService.demoteManagerToMember(memberActionRequest)
            );

            assertEquals("The user is already a member", ex.getMessage());
            verify(userGroupRoleRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenDemotingOwner() {
            userGroupRole.setRole(GroupRole.OWNER);

            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> groupService.demoteManagerToMember(memberActionRequest)
            );

            assertEquals("Cannot demote the group owner", ex.getMessage());
            verify(userGroupRoleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeMemberFromGroup()")
    class RemoveMemberFromGroupTests {
        @Test
        void shouldRemoveMemberSuccessfully() {
            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            MessageResponse result = groupService.removeMemberFromGroup(1L, "member");

            assertEquals("User removed from group successfully", result.getMessage());
            verify(userGroupRoleRepository).delete(userGroupRole);
        }

        @Test
        void shouldThrowWhenRemovingOwner() {
            userGroupRole.setRole(GroupRole.OWNER);

            when(groupUtil.findUser("member")).thenReturn(memberUser);
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.findUserRole(memberUser, group)).thenReturn(userGroupRole);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> groupService.removeMemberFromGroup(1L, "member")
            );

            assertEquals("Cannot remove the group owner", ex.getMessage());
            verify(userGroupRoleRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateGroupDetails()")
    class UpdateGroupDetailsTests {
        @Test
        void shouldUpdateGroupDetailsSuccessfully() {
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.updateField(eq("Updated Group"), any())).thenReturn(true);
            when(groupUtil.updateField(eq("Updated Description"), any())).thenReturn(true);

            MessageResponse result = groupService.updateGroupDetails(updateGroupRequest);

            assertEquals("Group details updated successfully", result.getMessage());
            assertEquals(Visibility.PRIVATE, group.getVisibility());
            verify(groupRepository).save(group);
        }

        @Test
        void shouldThrowWhenNoFieldsUpdated() {
            UpdateGroupRequest emptyRequest = new UpdateGroupRequest();
            emptyRequest.setGroupId(1L);

            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(groupUtil.updateField(isNull(), any())).thenReturn(false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> groupService.updateGroupDetails(emptyRequest)
            );

            assertEquals("No valid fields provided for update", ex.getMessage());
            verify(groupRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getManagedGroups()")
    class GetManagedGroupsTests {
        @Test
        void shouldReturnManagedGroups() {
            Set<Group> groups = Set.of(group);

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(groupRepository.findManagedGroupsByUserId(10L)).thenReturn(groups);

            Set<ManagedGroupsResponse> result = groupService.getManagedGroups();

            assertEquals(1, result.size());
            assertTrue(result.stream().anyMatch(g -> g.getId().equals("1") && g.getName().equals("Test Group")));
            verify(groupRepository).findManagedGroupsByUserId(10L);
        }
    }

    @Nested
    @DisplayName("viewContestsInGroup()")
    class ViewContestsInGroupTests {
        @Test
        void shouldReturnContestsInGroup() {
            Page<Contest> contestsPage = new PageImpl<>(List.of(contest), pageable, 1);

            when(contestRepository.findByGroupId(1L, pageable)).thenReturn(contestsPage);
            when(contestMapper.toGroupContestDto(contest)).thenReturn(groupContestsResponse);

            Page<GroupContestsResponse> result = groupService.viewContestsInGroup(1L, pageable);

            assertEquals(1, result.getTotalElements());
            verify(contestRepository).findByGroupId(1L, pageable);
            verify(contestMapper).toGroupContestDto(contest);
        }
    }

    @Nested
    @DisplayName("updateGroupPicture()")
    class UpdateGroupPictureTests {
        @Test
        void shouldUpdateGroupPictureSuccessfully() throws Exception {
            byte[] bytes = "image".getBytes();
            Map<String, Object> uploadResult = Map.of("secure_url", "https://cdn.example.com/group-new.jpg");

            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getBytes()).thenReturn(bytes);
            when(uploader.upload(eq(bytes), anyMap())).thenReturn(uploadResult);

            MessageResponse result = groupService.updateGroupPicture(updateGroupPictureRequest);

            assertEquals("Group picture updated successfully", result.getMessage());
            assertEquals("https://cdn.example.com/group-new.jpg", group.getPictureUrl());

            verify(imageService).checkPictureType(multipartFile);
            verify(imageService).deleteImageFromCloudinary("https://cdn.example.com/group-old.jpg", "groups/pictures");
            verify(groupRepository).save(group);
        }

        @Test
        void shouldUploadWithoutDeletingOldPictureWhenNull() throws Exception {
            group.setPictureUrl(null);
            byte[] bytes = "image".getBytes();
            Map<String, Object> uploadResult = Map.of("secure_url", "https://cdn.example.com/group-new.jpg");

            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getBytes()).thenReturn(bytes);
            when(uploader.upload(eq(bytes), anyMap())).thenReturn(uploadResult);

            MessageResponse result = groupService.updateGroupPicture(updateGroupPictureRequest);

            assertEquals("Group picture updated successfully", result.getMessage());
            verify(imageService, never()).deleteImageFromCloudinary(anyString(), anyString());
            verify(groupRepository).save(group);
        }

        @Test
        void shouldThrowRuntimeExceptionWhenUploadFails() throws Exception {
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            when(cloudinary.uploader()).thenReturn(uploader);
            when(multipartFile.getBytes()).thenThrow(new IOException("upload failed"));

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> groupService.updateGroupPicture(updateGroupPictureRequest)
            );

            assertEquals("Failed to upload group picture", ex.getMessage());
            verify(groupRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("viewGroupPicture()")
    class ViewGroupPictureTests {
        @Test
        void shouldReturnGroupPictureUrl() {
            when(groupUtil.findGroupById(1L)).thenReturn(group);

            PictureUrlResponse result = groupService.viewGroupPicture(1L);

            assertEquals("https://cdn.example.com/group-old.jpg", result.getPictureUrl());
            verify(groupUtil).findGroupById(1L);
        }
    }

    @Nested
    @DisplayName("deleteGroupPicture()")
    class DeleteGroupPictureTests {
        @Test
        void shouldDeleteGroupPictureSuccessfully() throws IOException {
            when(groupUtil.findGroupById(1L)).thenReturn(group);

            MessageResponse result = groupService.deleteGroupPicture(1L);

            assertEquals("Your profile picture has been successfully deleted", result.getMessage());
            assertNull(group.getPictureUrl());

            verify(groupRepository).save(group);
            verify(imageService).deleteImageFromCloudinary("https://cdn.example.com/group-old.jpg", "groups/pictures");
        }

        @Test
        void shouldThrowWhenGroupHasNoPicture() {
            group.setPictureUrl(" ");
            when(groupUtil.findGroupById(1L)).thenReturn(group);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> groupService.deleteGroupPicture(1L)
            );

            assertEquals("Group does not have a profile picture", ex.getMessage());
            verify(groupRepository, never()).save(any());
        }

        @Test
        void shouldStillReturnSuccessWhenCloudinaryDeletionFails() throws IOException {
            when(groupUtil.findGroupById(1L)).thenReturn(group);
            doThrow(new RuntimeException("cloudinary error"))
                    .when(imageService)
                    .deleteImageFromCloudinary("https://cdn.example.com/group-old.jpg", "groups/pictures");

            MessageResponse result = groupService.deleteGroupPicture(1L);

            assertEquals("Your profile picture has been successfully deleted", result.getMessage());
            assertNull(group.getPictureUrl());
            verify(groupRepository).save(group);
        }
    }

    @Nested
    @DisplayName("searchContestByName()")
    class SearchContestByNameTests {
        @Test
        void shouldSearchContestByName() {
            Page<Contest> contestsPage = new PageImpl<>(List.of(contest), pageable, 1);

            doReturn(contestsPage)
                    .when(contestRepository)
                    .findAll(any(Specification.class), eq(pageable));

            when(contestMapper.toGroupContestDto(contest)).thenReturn(groupContestsResponse);

            Page<GroupContestsResponse> result = groupService.searchContestByName(1L, "Contest", pageable);

            assertEquals(1, result.getTotalElements());
            verify(contestRepository).findAll(any(Specification.class), eq(pageable));
            verify(contestMapper).toGroupContestDto(contest);
        }

        @Test
        void shouldHandleNullSpecInSearchContestByName() {
            Page<Contest> contestsPage = new PageImpl<>(List.of(contest), pageable, 1);

            doReturn(contestsPage)
                    .when(contestRepository)
                    .findAll(any(Specification.class), eq(pageable));

            when(contestMapper.toGroupContestDto(contest)).thenReturn(groupContestsResponse);

            Page<GroupContestsResponse> result = groupService.searchContestByName(null, null, pageable);

            assertEquals(1, result.getTotalElements());
            verify(contestRepository).findAll(any(Specification.class), eq(pageable));
        }
    }
}