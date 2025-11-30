package com.icoder.problem.management.controller;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.service.implementation.ProblemServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemServiceImpl problemService;

    @GetMapping("/{judge_type}/{problem_code}")
    @Operation(summary = "Get a specific problem", description = "Returns a specific problem statement by its online judge and problem code")
    public ResponseEntity<ProblemStatementResponse> getProblem(@PathVariable("judge_type") String source,
                                                               @PathVariable("problem_code") String code) {

        return ResponseEntity.ok(problemService.getProblemStatement(source, code));
    }

    @GetMapping("/{judge_type}/{problem_code}/metadata")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get metadata of a specific problem", description = "Returns a specific problem metadata by its online judge and problem code " +
            "(e.g., problem title, problem link, contest title, ...)")
    public ResponseEntity<ProblemResponse> getProblemMetadata(@PathVariable("judge_type") String source,
                                                               @PathVariable("problem_code") String code) {

        return ResponseEntity.ok(problemService.getProblemMetadata(source, code));
    }

    @GetMapping
    @Operation(summary = "Get all problems", description = "Returns a paginated list of all problems")
    public ResponseEntity<Page<ProblemResponse>> getAllProblems(
            @SortDefault(
                    sort = "fetchedAt", direction = Sort.Direction.DESC
            )
            Pageable pageable) {
        return ResponseEntity.ok(problemService.getAllProblems(pageable));
    }
}
