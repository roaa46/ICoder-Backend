package com.icoder.meeting.management.service.interfaces;

import com.icoder.meeting.management.dto.CreateMeetingRequest;
import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.dto.QuickSessionRequest;

import java.util.List;

public interface MeetingService {

    MeetingResponse createOfficialMeeting(CreateMeetingRequest request);

    MeetingResponse createQuickSession(QuickSessionRequest request);

    List<MeetingResponse> getGroupMeetings(Long groupId);

    MeetingResponse joinMeeting(String roomName);

    void endMeeting(Long meetingId);
}