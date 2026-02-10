package com.icoder.submission.management.controller;

import com.icoder.submission.management.dto.LanguageOptionResponse;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping("/languages/cses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LanguageOptionResponse>> getCsesLanguage() {
        return ResponseEntity.ok(submissionService.getCsesLanguages());
    }

    @GetMapping("/languages/codeforces")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LanguageOptionResponse>> getCodeforcesLanguage() {
        return ResponseEntity.ok(submissionService.getCodeforcesLanguages());
    }

    @GetMapping("/languages/atcoder")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LanguageOptionResponse>> getAtCoderLanguage() {
        return ResponseEntity.ok(submissionService.getAtCoderLanguages());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionCreateResponse> submitCode(@Valid @RequestBody SubmissionCreateRequest request) {
        return ResponseEntity.ok(submissionService.submit(request));
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionCreateResponse> getSubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getSubmission(submissionId));
    }
}
