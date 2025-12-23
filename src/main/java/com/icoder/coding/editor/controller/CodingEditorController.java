package com.icoder.coding.editor.controller;

import com.icoder.coding.editor.dto.*;
import com.icoder.coding.editor.service.interfaces.CodingEditorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
