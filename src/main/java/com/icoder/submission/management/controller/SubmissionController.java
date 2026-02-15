package com.icoder.submission.management.controller;

import com.icoder.submission.management.dto.*;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import com.icoder.submission.management.service.interfaces.SubmissionStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v2/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission V2", description = "The endpoints related to submission operations (v2)")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final SubmissionStreamService streamService;

    @GetMapping("/languages/{oj}")
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

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter streamSubmission(@PathVariable Long id) {
        return streamService.createEmitter(id);
    }
}
