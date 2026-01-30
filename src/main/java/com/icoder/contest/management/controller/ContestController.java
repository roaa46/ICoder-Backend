package com.icoder.contest.management.controller;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.core.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;

    @PostMapping
    public ResponseEntity<MessageResponse> createContest(@Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.createContest(request));
    }

    @PutMapping("/{contestId}")
    public ResponseEntity<MessageResponse> updateContest(@PathVariable Long contestId, @Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok(contestService.updateContest(contestId, request));
    }

    @DeleteMapping("/{contestId}")
    public ResponseEntity deleteContest(@PathVariable Long contestId, @RequestParam("group_id") Long groupId) {
        contestService.deleteContest(contestId, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Page<GroupContestsResponse>> getContestsInGroup(@PathVariable Long groupId,
                                                                          @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(contestService.viewContestsInGroup(groupId, pageable));
    }
}
