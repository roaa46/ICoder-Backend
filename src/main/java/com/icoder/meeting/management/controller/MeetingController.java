package com.icoder.meeting.management.controller;

import com.icoder.meeting.management.dto.CreateMeetingRequest;
import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.dto.QuickSessionRequest;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.service.interfaces.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<MeetingResponse>> getGroupMeetings(
            @PathVariable Long groupId,
            @RequestParam(required = false, value = "status") MeetingStatus status,
            @PageableDefault(size = 5, sort = "scheduledStartTime", direction = Sort.Direction.DESC) Pageable paging
    ) {
        return ResponseEntity.ok(
                meetingService.getGroupMeetings(groupId, status, paging)
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
