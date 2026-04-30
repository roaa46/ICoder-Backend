package com.icoder.coding.editor.controller;

import com.icoder.coding.editor.dto.*;
import com.icoder.coding.editor.service.interfaces.CodingEditorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coding/editor")
@RequiredArgsConstructor
public class CodingEditorController {
    public static final int FINAL_STATUS_THRESHOLD = 3;
    private final CodingEditorService codingEditorService;

    @GetMapping("/language")
    @Operation(
            summary = "Retrieve language details by ID",
            description = "Fetches detailed information (name, version) for a specific coding language " +
                    "supported by Judge0 using its unique ID (doesn't require login)"
    )
    public ResponseEntity<LanguageResponse> getLanguage(@RequestParam Integer id) {
        return ResponseEntity.ok(codingEditorService.getLanguage(id));
    }

    @GetMapping("/languages")
    @Operation(
            summary = "Retrieve all supported coding languages",
            description = "Returns a complete list of languages and their respective IDs available for code submission" +
                    " on the Judge0 platform (doesn't require login)"
    )
    public ResponseEntity<List<LanguageResponse>> getLanguages() {
        return ResponseEntity.ok(codingEditorService.getLanguages());
    }

    @PostMapping("/submissions")
    @Operation(
            summary = "Submit a single code snippet for execution",
            description = "Accepts the source code, language ID, and standard input (stdin). A unique token is returned to poll for results (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenResponse> submitCode(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(codingEditorService.submitCode(request));
    }

    @GetMapping("/submissions/{token}")
    @Operation(
            summary = "Retrieve the execution result (Polling Endpoint)",
            description = "Retrieves the current status and output of a submission. \n\n" +
                    "**Frontend Workflow:** \n" +
                    "1. If this endpoint returns **202 Accepted**, it means the code is still in queue or processing. The frontend should wait (e.g., 1-2 seconds) and poll again. \n" +
                    "2. If it returns **200 OK**, the execution is complete (regardless of whether the result is 'Accepted', 'Wrong Answer', or 'Error'). \n\n" +
                    "**Note:** Use the `status.id` to distinguish between 'Processing' (< 3) and 'Final' states (>= 3)."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubmissionResult> getResult(@PathVariable String token) {
        SubmissionResult result = codingEditorService.processAndGetResult(token);

        if (result.getStatus() != null && result.getStatus().getId() < FINAL_STATUS_THRESHOLD) {
            return ResponseEntity.accepted().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/submissions/batch")
    @Operation(
            summary = "Submit multiple test cases for a single code snippet in a batch",
            description = "Processes a batch request containing source code and multiple test inputs (input/expected output). Returns a list of tokens, one for each submitted test case (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TokenResponse>> batchedSubmitCode(@RequestBody BatchRunRequest request) {
        return ResponseEntity.ok(codingEditorService.submitBatchCode(request));
    }

    @GetMapping("/submissions/batch")

    @Operation(
            summary = "Retrieve execution results for multiple submissions (Batch Polling Endpoint)",
            description = "Retrieves the current status and outputs for a batch of submission tokens. \n\n" +
                    "**Frontend Workflow:** \n" +
                    "1. If this endpoint returns **202 Accepted**, it means **at least one submission** is still in queue or processing. The frontend should wait (e.g., 1-2 seconds) and poll again. \n" +
                    "2. If it returns **200 OK**, all submissions have finished execution (regardless of whether results are 'Accepted', 'Wrong Answer', or 'Error'). \n\n" +
                    "**Note:** Use the `status.id` to distinguish between 'Processing' (< 3) and 'Final' states (>= 3)."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BatchSubmissionResult> getBatchResults(@RequestParam List<String> tokens) {

        BatchSubmissionResult results = codingEditorService.processAndGetBatchResults(tokens);

        boolean isStillProcessing = results.getSubmissions().stream()
                .anyMatch(r -> r != null && r.getStatus().getId() < FINAL_STATUS_THRESHOLD);

        if (isStillProcessing) {
            return ResponseEntity.accepted().body(results);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/templates")
    @Operation(
            summary = "Add a new code template",
            description = "Creates a new code template and returns its details along with the resource location (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity addTemplate(@RequestBody CodeTemplateRequest request) {

        CodeTemplateResponse response =
                codingEditorService.addTemplate(request);

        URI location = URI.create("/templates/" + response.getTemplateId());

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/templates/{id}/toggle")
    @Operation(
            summary = "Toggle Template Status",
            description = "Toggles the 'enabled' state of a template. " +
                    "If 'force' is false and another template for the same language is already active, it returns a 409 Conflict. " +
                    "If 'force' is true, it deactivates the existing template and activates this one."
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CodeTemplateResponse> toggleTemplate(@PathVariable Long id, @RequestParam boolean force) {
        return ResponseEntity.ok(codingEditorService.toggleTemplate(id, force));
    }

    @GetMapping("/templates/{id}")
    @Operation(
            summary = "Retrieve a code template by ID",
            description = "Returns the details of a single code template using its unique ID (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CodeTemplateResponse> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(codingEditorService.getTemplate(id));
    }

    @GetMapping("/templates")
    @Operation(
            summary = "Retrieve all code templates",
            description = "Returns a paginated list of all code templates (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CodeTemplateResponse>> getAllTemplates(
            @RequestParam(defaultValue = "0") int page) {
        Page<CodeTemplateResponse> templates = codingEditorService.getTemplates(page);
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/templates/{id}")
    @Operation(
            summary = "Update an existing code template",
            description = "Updates the details of a code template using its ID and returns the updated template (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CodeTemplateResponse> editTemplate(@PathVariable Long id, @RequestBody CodeTemplateRequest request) {
        return ResponseEntity.ok(codingEditorService.editTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(
            summary = "Delete a code template",
            description = "Deletes the code template identified by the given ID (requires login)"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity deleteTemplate(@PathVariable Long id) {
        codingEditorService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}