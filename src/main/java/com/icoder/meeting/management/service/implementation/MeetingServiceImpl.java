package com.icoder.meeting.management.service.implementation;

import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.*;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.repository.*;
import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.enums.InvitationType;
import com.icoder.invitation.management.repository.InvitationRepository;
import com.icoder.meeting.management.dto.*;
import com.icoder.meeting.management.entity.Meeting;
import com.icoder.meeting.management.enums.*;
import com.icoder.meeting.management.mapper.MeetingMapper;
import com.icoder.meeting.management.repository.MeetingRepository;
import com.icoder.meeting.management.service.interfaces.MeetingService;
import com.icoder.notification.management.entity.Notification;
import com.icoder.notification.management.enums.NotificationType;
import com.icoder.notification.management.repository.NotificationRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final GroupRepository groupRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final NotificationRepository notificationRepository;
    private final InvitationRepository invitationRepository;
    private final SecurityUtils securityUtils;
    private final MeetingMapper meetingMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MeetingResponse createOfficialMeeting(CreateMeetingRequest request) {

        User currentUser = getCurrentUser();

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        validateCoordinator(currentUser, group);

        if (!request.getInstant()) {

            if (request.getScheduledStartTime() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Scheduled time is required"
                );
            }

            if (request.getScheduledStartTime().isBefore(Instant.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Meeting time must be in future"
                );
            }
        }

        Contest contest = null;
        String generatedTitle;

        if (request.getMeetingType() == MeetingType.GENERAL) {
            generatedTitle = request.getTitle();
        }
        else if (request.getMeetingType() == MeetingType.HELPDESK) {

            contest = contestRepository.findById(request.getContestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));

            if (contest.getContestStatus() != ContestStatus.RUNNING) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Contest must be running"
                );
            }

            boolean exists = meetingRepository
                    .existsByRelatedContestIdAndTypeAndStatusIn(
                            contest.getId(),
                            MeetingType.HELPDESK,
                            List.of(MeetingStatus.SCHEDULED, MeetingStatus.ONGOING)
                    );

            if (exists) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Helpdesk already exists for this contest"
                );
            }

            generatedTitle = contest.getTitle() + " Helpdesk";

        } else {

            contest = contestRepository.findById(request.getContestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));

            if (contest.getContestStatus() != ContestStatus.ENDED) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Contest must have been ended"
                );
            }

            long editorialCount =
                    meetingRepository.countByRelatedContestIdAndType(
                            contest.getId(),
                            MeetingType.EDITORIAL
                    );

            generatedTitle =
                    contest.getTitle() +
                            " Editorial " +
                            (editorialCount + 1);
        }

        Meeting meeting = Meeting.builder()
                .title(generatedTitle)
                .roomName(UUID.randomUUID().toString())
                .type(request.getMeetingType())
                .status(Boolean.TRUE.equals(request.getInstant())
                        ? MeetingStatus.ONGOING
                        : MeetingStatus.SCHEDULED)
                .official(true)
                .scheduledStartTime(request.getScheduledStartTime())
                .group(group)
                .relatedContest(contest)
                .creator(currentUser)
                .build();

        meeting = meetingRepository.save(meeting);

        broadcastMeetingCreated(meeting);

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public MeetingResponse createQuickSession(QuickSessionRequest request) {

        User currentUser = getCurrentUser();

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        validateMembership(currentUser.getId(), group.getId());

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .roomName(UUID.randomUUID().toString())
                .type(MeetingType.QUICK_SESSION)
                .status(MeetingStatus.ONGOING)
                .official(false)
                .group(group)
                .creator(currentUser)
                .build();

        meeting = meetingRepository.save(meeting);

        broadcastMeetingCreated(meeting);

        return meetingMapper.toResponse(meeting);
    }

    @Override
    public List<MeetingResponse> getGroupMeetings(Long groupId) {

        User currentUser = getCurrentUser();

        validateMembership(currentUser.getId(), groupId);

        return meetingRepository.findByGroupId(groupId)
                .stream()
                .map(meetingMapper::toResponse)
                .toList();
    }

    @Override
    public MeetingResponse joinMeeting(String roomName) {

        User currentUser = getCurrentUser();

        Meeting meeting = meetingRepository.findByRoomName(roomName)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        validateMembership(currentUser.getId(), meeting.getGroup().getId());

        if (meeting.getStatus() == MeetingStatus.SCHEDULED) {

            Instant allowedTime =
                    meeting.getScheduledStartTime().minusSeconds(300);

            if (Instant.now().isBefore(allowedTime)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Meeting is not available yet"
                );
            }

            if (Instant.now().isAfter(meeting.getScheduledStartTime())) {
                meeting.setStatus(MeetingStatus.ONGOING);
                meetingRepository.save(meeting);
            }
        }

        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Meeting already ended"
            );
        }

        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional
    public void endMeeting(Long meetingId) {

        User currentUser = getCurrentUser();

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));

        if (!meeting.getCreator().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only creator can end meeting"
            );
        }

        meeting.setStatus(MeetingStatus.ENDED);
        meeting.setEndedAt(Instant.now());

        meetingRepository.save(meeting);

        messagingTemplate.convertAndSend(
                "/topic/group/" + meeting.getGroup().getId(),
                "Meeting ended: " + meeting.getTitle()
        );
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void startScheduledMeetings() {

        List<Meeting> meetings =
                meetingRepository.findAllByStatusAndScheduledStartTimeBefore(
                        MeetingStatus.SCHEDULED,
                        Instant.now()
                );

        if (meetings.isEmpty()) {
            return;
        }

        for (Meeting meeting : meetings) {

            meeting.setStatus(MeetingStatus.ONGOING);

            messagingTemplate.convertAndSend(
                    "/topic/group/" + meeting.getGroup().getId(),
                    "Meeting started: " + meeting.getTitle()
            );
        }

        meetingRepository.saveAll(meetings);
    }

    private void broadcastMeetingCreated(Meeting meeting) {

        Notification notification = Notification.builder()
                .recipient(meeting.getCreator())
                .targetId(meeting.getId())
                .message("New meeting created: " + meeting.getTitle())
                .type(NotificationType.MEETING_REMINDER)
                .actionUrl("/meetings/" + meeting.getRoomName())
                .build();

        notificationRepository.save(notification);

        messagingTemplate.convertAndSend(
                "/topic/group/" + meeting.getGroup().getId(),
                meetingMapper.toResponse(meeting)
        );
    }


    private User getCurrentUser() {

        Long currentUserId = securityUtils.getCurrentUserId();

        return userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private void validateMembership(Long userId, Long groupId) {

        boolean member =
                userGroupRoleRepository.existsByUserIdAndGroupId(
                        userId,
                        groupId
                );

        if (!member) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a group member"
            );
        }
    }

    private void validateCoordinator(User user, Group group) {

        UserGroupRole relation =
                userGroupRoleRepository.findByUserAndGroup(user, group)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You are not a member"
                                )
                        );

        if (relation.getRole() != GroupRole.OWNER &&
                relation.getRole() != GroupRole.MANAGER) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only coordinators can create official meetings"
            );
        }
    }
}