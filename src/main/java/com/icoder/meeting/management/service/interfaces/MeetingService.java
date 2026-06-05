package com.icoder.meeting.management.service.interfaces;

import com.icoder.meeting.management.dto.CreateMeetingRequest;
import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.dto.QuickSessionRequest;
import com.icoder.meeting.management.enums.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingService {

    MeetingResponse createOfficialMeeting(CreateMeetingRequest request);

    MeetingResponse createQuickSession(QuickSessionRequest request);

    Page<MeetingResponse> getGroupMeetings(Long groupId, MeetingStatus status, Pageable pageable);

    MeetingResponse joinMeeting(String roomName);

    void endMeeting(Long meetingId);
}