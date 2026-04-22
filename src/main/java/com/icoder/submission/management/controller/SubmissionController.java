package com.icoder.submission.management.controller;

import com.icoder.submission.management.dto.*;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission V1", description = "The endpoints related to submission operations (v2)")
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping("/languages/{oj}")
    @Operation(summary = "Get languages for online judge", description = "Retrieve list of languages supported by online judge")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LanguageOptionResponse>> getLanguages(@PathVariable String oj) {
        return ResponseEntity.ok(submissionService.getLanguages(oj));
    }

    @GetMapping
    @Operation(summary = "Get all submissions with filters (it could be used to reset too)", description = "Retrieve paginated list of all submissions filtered by ser handle, online judge, problem code, and language")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SubmissionPageResponse>> getAllSubmissions(
            @RequestParam(required = false, value = "handle") String userHandle,
            @RequestParam(required = false, value = "online_judge") String oj,
            @RequestParam(required = false, value = "problem_code") String problemCode,
            @RequestParam(required = false) String language,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable paging) {
        Page<SubmissionPageResponse> submissions = submissionService.getAllSubmissions(userHandle, oj, problemCode, language, paging);
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

    @PostMapping
    @Operation(summary = "Submit code", description = "Submit code solution for a problem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionCreateResponse> submitCode(
            @Valid @RequestBody SubmissionCreateRequest request) {
        SubmissionCreateResponse response = submissionService.submit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/session")
    @Operation(summary = "Add session id", description = "add session id to submit with user account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionSubmissionResponse> addSession(
            @Valid @RequestBody SessionSubmissionRequest request) {
        return new ResponseEntity<>(submissionService.addSessionId(request), HttpStatus.CREATED);
    }

    @PostMapping("/session/update")
    @Operation(summary = "Update user session id", description = "update user session id to submit with user account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionSubmissionResponse> updateSession(@Valid @RequestBody SessionSubmissionRequest request) {
        return new ResponseEntity<>(submissionService.updateSession(request), HttpStatus.CREATED);
    }

    @GetMapping("/session/{judgeType}")
    @Operation(summary = "Get user session by judge type", description = "get ")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionSubmissionResponse> getSession(@PathVariable String judgeType) {
        return new ResponseEntity<>(submissionService.getSession(judgeType), HttpStatus.OK);
    }

    @DeleteMapping("/session/{id}")
    @Operation(summary = "Delete user session user", description = "")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity deleteSession(@PathVariable Long id) {
        submissionService.deleteSession(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{submissionId}/toogle-openness")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> toggleOpenness(@PathVariable @RequestParam Long submissionId) {
        return new ResponseEntity<>(submissionService.updateSubmissionOpen(submissionId), HttpStatus.NO_CONTENT);
    }
}
