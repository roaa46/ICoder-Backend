package com.icoder.contest.management.controller;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.core.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;
    @PostMapping
    public ResponseEntity<MessageResponse> createContest(@Valid @RequestBody CreateContestRequest request) {
        return ResponseEntity.ok( contestService.createContest(request));
    }
}
