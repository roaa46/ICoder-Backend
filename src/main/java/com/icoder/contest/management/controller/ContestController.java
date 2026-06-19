package com.icoder.contest.management.controller;

import com.icoder.contest.management.dto.*;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.service.interfaces.LeaderboardService;
import com.icoder.core.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/contests")
@Tag(name = "Contest Management")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;
    private final LeaderboardService leaderboardService;

    @PostMapping
    @PreAuthorize(value = "isAuthenticated()")
    @Operation(summary = "Create a new contest", description = "Creates a new contest with the provided details. Contest coordinators only can create contests.")
    public ResponseEntity<MessageResponse> createContest(@Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.createContest(request));
    }

    @PutMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    @Operation(summary = "Update an existing contest", description = "Updates an existing contest with the provided details. Contest coordinators only can update contests.")
    public ResponseEntity<MessageResponse> updateContest(@PathVariable Long contestId, @Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.updateContest(contestId, request));
    }

    @DeleteMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    @Operation(summary = "Delete a contest", description = "Deletes an existing contest. Contest coordinators only can delete contests.")
    public ResponseEntity deleteContest(@PathVariable Long contestId) {
        contestService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    @Operation(summary = "Get contest details", description = "Retrieves the details of a specific contest.")
    public ResponseEntity<ContestDetailsResponse> getContestDetails(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.viewContestDetails(contestId));
    }

    @GetMapping("/{contestId}/problems")
    @PreAuthorize(value = "isAuthenticated()")
    @Operation(summary = "Get contest problems", description = "Retrieves the problems of a specific contest.")
    public ResponseEntity<Set<ProblemSetResponse>> getProblemSet(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.viewProblemSet(contestId));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all contests", description = "Retrieves a list of all contests with filters.")
    public ResponseEntity<Page<ContestResponse>> getAllContests(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, value = "group_name") String groupName,
            @RequestParam(required = false) ContestStatus status,
            @RequestParam(required = false, value = "contest_openness") ContestOpenness openness,
            @SortDefault(sort = "beginTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(contestService.viewAllContests(title, groupName, status, openness, pageable));
    }

    @PostMapping("/protected/{contestId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Join a protected contest", description = "Allows authenticated users who are not members of the contest's group (non-group members) to join a protected contest using the contest password.")
    public ResponseEntity<MessageResponse> joinProtectedContest(@PathVariable Long contestId, @RequestBody JoinProtectedContestRequest request) {
        return ResponseEntity.ok(contestService.joinProtectedContest(contestId, request));
    }

    @GetMapping("/protected/{userId}/{groupId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if a user is a member of a protected contest group", description = "Checks if a user is a member of a protected contest group.")
    public ResponseEntity<MessageResponse> checkProtectedContestMembership(@PathVariable Long userId, @PathVariable Long groupId) {
        return ResponseEntity.ok(contestService.checkProtectedContestMembership(userId, groupId));
    }

    @GetMapping("{contestId}/leaderboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contest leaderboard", description = "Retrieves the leaderboard of a specific contest.")
    public ResponseEntity<List<LeaderboardRowResponse>> getLeaderboard(@PathVariable Long contestId) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(contestId));
    }

    @GetMapping(value = "{contestId}/leaderboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Stream contest leaderboard", description = "Streams the leaderboard of a specific contest.")
    public SseEmitter streamLeaderboard(@PathVariable Long contestId) {
        return leaderboardService.subscribeToLeaderboard(contestId);
    }
}
