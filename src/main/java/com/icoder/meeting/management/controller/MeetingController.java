package com.icoder.meeting.management.controller;

import com.icoder.meeting.management.dto.CreateMeetingRequest;
import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.dto.QuickSessionRequest;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.service.interfaces.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Meeting Management")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/official")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create an official meeting", description = "Creates an official meeting with the provided details.")
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
    @Operation(summary = "Create a quick session", description = "Creates a quick session with the provided details.")
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
    @Operation(summary = "Get group meetings", description = "Retrieves all meetings for a specific group (official & quick session).")
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
    @Operation(summary = "Join a meeting", description = "Joins a meeting with the provided room name.")
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
    @Operation(summary = "End a meeting", description = "Ends a meeting with the provided meeting ID.")
    public ResponseEntity<Void> endMeeting(
            @PathVariable
            Long meetingId
    ) {
        meetingService.endMeeting(meetingId);
        return ResponseEntity.ok().build();
    }
}
