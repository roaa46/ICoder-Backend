package com.icoder.coding.editor.controller;

import com.icoder.coding.editor.dto.*;
import com.icoder.coding.editor.service.interfaces.Judge0Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/judge0")
@RequiredArgsConstructor
public class Judge0Controller {
    private final Judge0Service judge0Service;
    public static final int FINAL_STATUS_THRESHOLD = 3;

    @GetMapping("/language")
    public ResponseEntity<LanguageResponse> getLanguage(@RequestParam Integer id) {
        return ResponseEntity.ok(judge0Service.getLanguage(id));
    }

    @GetMapping("/languages")
    public ResponseEntity<List<LanguageResponse>> getLanguages() {
        return ResponseEntity.ok(judge0Service.getLanguages());
    }

    @PostMapping("/submissions")
    public ResponseEntity<TokenResponse> submitCode(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(judge0Service.submitCode(request));
    }

    @GetMapping("/submissions/{token}")
    public ResponseEntity<SubmissionResult> getResult(@PathVariable String token) {

        SubmissionResult result = judge0Service.processAndGetResult(token);

        if (result.getStatus() != null && result.getStatus().getId() < FINAL_STATUS_THRESHOLD) {
            return ResponseEntity.accepted().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
