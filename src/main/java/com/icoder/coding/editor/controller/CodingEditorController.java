package com.icoder.coding.editor.controller;

import com.icoder.coding.editor.dto.*;
import com.icoder.coding.editor.service.interfaces.CodingEditorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coding/editor")
@RequiredArgsConstructor
public class CodingEditorController {
    private final CodingEditorService codingEditorService;
    public static final int FINAL_STATUS_THRESHOLD = 3;

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
    public ResponseEntity<TokenResponse> submitCode(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(codingEditorService.submitCode(request));
    }

    @GetMapping("/submissions/{token}")
    @Operation(
            summary = "Retrieve the execution result for a single submission token",
            description = "Polls the Judge0 service using the provided token. If processing is still ongoing, returns 202 Accepted. If finished, returns 200 OK with the final result (output, status, time, memory) (requires login)"
    )
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
    public ResponseEntity<List<TokenResponse>> batchedSubmitCode(@RequestBody BatchRunRequest request) {
        return ResponseEntity.ok(codingEditorService.submitBatchCode(request));
    }

    @GetMapping("/submissions/batch")
    @Operation(
            summary = "Retrieve the execution results for a batch of submission tokens",
            description = "Accepts a list of tokens. Checks the status of all submissions. Returns 202 Accepted if any submission is still running, or 200 OK if all submissions have finished processing (requires login)"
    )
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
    public ResponseEntity<CodeTemplateResponse> toggleTemplate(@PathVariable Long id, @RequestParam boolean force){
        return ResponseEntity.ok(codingEditorService.toggleTemplate(id, force));
    }

    @GetMapping("/templates/{id}")
    @Operation(
            summary = "Retrieve a code template by ID",
            description = "Returns the details of a single code template using its unique ID (requires login)"
    )
    public ResponseEntity<CodeTemplateResponse> getTemplate(@PathVariable String id) {
        return ResponseEntity.ok(codingEditorService.getTemplate(id));
    }

    @GetMapping("/templates")
    @Operation(
            summary = "Retrieve all code templates",
            description = "Returns a paginated list of all code templates (requires login)"
    )
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
    public ResponseEntity<CodeTemplateResponse> editTemplate(@PathVariable String id, @RequestBody CodeTemplateRequest request) {
        return ResponseEntity.ok(codingEditorService.editTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(
            summary = "Delete a code template",
            description = "Deletes the code template identified by the given ID (requires login)"
    )
    public ResponseEntity deleteTemplate(@PathVariable String id) {
        codingEditorService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}