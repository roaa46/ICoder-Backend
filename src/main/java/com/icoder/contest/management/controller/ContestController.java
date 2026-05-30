package com.icoder.contest.management.controller;

import com.icoder.contest.management.dto.*;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.service.interfaces.LeaderboardService;
import com.icoder.core.dto.MessageResponse;
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
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;
    private final LeaderboardService leaderboardService;

    @PostMapping
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> createContest(@Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.createContest(request));
    }

    @PutMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<MessageResponse> updateContest(@PathVariable Long contestId, @Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.updateContest(contestId, request));
    }

    @DeleteMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity deleteContest(@PathVariable Long contestId) {
        contestService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contestId}")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<ContestDetailsResponse> getContestDetails(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.viewContestDetails(contestId));
    }

    @GetMapping("/{contestId}/problems")
    @PreAuthorize(value = "isAuthenticated()")
    public ResponseEntity<Set<ProblemSetResponse>> getProblemSet(@PathVariable Long contestId) {
        return ResponseEntity.ok(contestService.viewProblemSet(contestId));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ContestResponse>> getAllContests(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, value = "group_name") String groupName,
            @RequestParam(required = false) ContestStatus status,
            @RequestParam(required = false) ContestType type,
            @SortDefault(sort = "beginTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(contestService.viewAllContests(title, groupName, status, type, pageable));
    }

    @GetMapping("{contestId}/leaderboard")
    public ResponseEntity<List<LeaderboardRowResponse>> getLeaderboard(@PathVariable Long contestId) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(contestId));
    }

    @GetMapping(value = "{contestId}/leaderboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLeaderboard(@PathVariable Long contestId) {
        return leaderboardService.subscribeToLeaderboard(contestId);
    }
}
