package com.icoder.submission.management.controller;

import com.icoder.submission.management.dto.*;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission V2", description = "The endpoints related to submission operations (v2)")
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping
    @Operation(summary = "Get all submissions", description = "Retrieve paginated list of all submissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SubmissionPageResponse>> getAllSubmissions(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "25") Integer size) {
        Pageable paging = PageRequest.of(pageNo, size);
        Page<SubmissionPageResponse> submissions = submissionService.getAllSubmissions(paging);
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @GetMapping(params = {"userHandle", "oj", "problemCode", "language"})
    @Operation(summary = "Filter submissions", description = "Filter submissions by user handle, online judge, problem code, and language")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SubmissionPageResponse>> filterSubmissions(
            @RequestParam(required = false, defaultValue = "") String userHandle,
            @RequestParam(required = false, defaultValue = "") String oj,
            @RequestParam(required = false, defaultValue = "") String problemCode,
            @RequestParam(required = false, defaultValue = "") String language,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "25") Integer size) {
        Pageable paging = PageRequest.of(pageNo, size);
        Page<SubmissionPageResponse> submissions = submissionService.filterSubmissions(
                userHandle, oj, problemCode, language, paging);
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission by ID", description = "Retrieve a specific submission by its ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable Long id) {
        SubmissionResponse submission = submissionService.getSubmissionById(id);
        return new ResponseEntity<>(submission, HttpStatus.OK);
    }

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
    @Operation(summary = "Submit code", description = "Submit code solution for a problem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionCreateResponse> submitCode(
            @Valid @RequestBody SubmissionCreateRequest request) {
        SubmissionCreateResponse response = submissionService.submit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionCreateResponse> getSubmission(@PathVariable Long submissionId) {
        return ResponseEntity.ok(submissionService.getSubmission(submissionId));
    }
}
