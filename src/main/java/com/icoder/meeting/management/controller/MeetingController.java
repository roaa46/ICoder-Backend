package com.icoder.meeting.management.controller;

import com.icoder.meeting.management.dto.CreateMeetingRequest;
import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.dto.QuickSessionRequest;
import com.icoder.meeting.management.service.interfaces.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/official")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> createOfficialMeeting(
            @Valid
            @RequestBody
            CreateMeetingRequest request
    ) {
        return ResponseEntity.ok(
                meetingService.createOfficialMeeting(request)
        );
    }

    @PostMapping("/quick-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> createQuickSession(
            @Valid
            @RequestBody
            QuickSessionRequest request
    ) {
        return ResponseEntity.ok(
                meetingService.createQuickSession(request)
        );
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MeetingResponse>> getGroupMeetings(
            @PathVariable
            Long groupId
    ) {
        return ResponseEntity.ok(
                meetingService.getGroupMeetings(groupId)
        );
    }

    @PostMapping("/join/{roomName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> joinMeeting(
            @PathVariable
            String roomName
    ) {
        return ResponseEntity.ok(
                meetingService.joinMeeting(roomName)
        );
    }

    @PatchMapping("/{meetingId}/end")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> endMeeting(
            @PathVariable
            Long meetingId
    ) {
        meetingService.endMeeting(meetingId);
        return ResponseEntity.ok().build();
    }
}
