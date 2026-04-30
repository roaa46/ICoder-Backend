package com.icoder.problem.management.controller;

import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.service.interfaces.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;

    @GetMapping("/{judge_type}/{problem_code}/metadata")
    @Operation(summary = "Get metadata of a specific problem", description = "Returns a specific problem metadata by its online judge and problem code " +
            "(e.g., problem title, problem link, contest title, ...) (requires login)")
    public ResponseEntity<ProblemResponse> getProblemMetadata(@PathVariable("judge_type") String source,
                                                              @PathVariable("problem_code") String code) {
        return ResponseEntity.ok(problemService.getProblemMetadata(source, code));
    }

    @GetMapping("/{judge_type}/{problem_code}")
    @Operation(summary = "Get a specific problem", description = "Returns a specific problem statement by its online judge and problem code (requires login)")
    public ResponseEntity<ProblemStatementResponse> getProblem(@PathVariable("judge_type") String source,
                                                               @PathVariable("problem_code") String code) {
        return ResponseEntity.ok(problemService.getProblemStatement(source, code));
    }

    @GetMapping("/recrawl/{judge_type}/{problem_code}")
    @Operation(summary = "Fetch a specific problem", description = "Fetches a specific problem statement by its online judge and problem code (requires login)")
    @CachePut(value = "problem_statement", key = "#p0 + ':' + #p1")
    public ResponseEntity<ProblemStatementResponse> fetchProblem(@PathVariable("judge_type") String source,
                                                                 @PathVariable("problem_code") String code) {
        return ResponseEntity.ok(problemService.getProblemStatement(source, code));
    }

    @GetMapping
    @Operation(
            summary = "Get all problems with optional filters",
            description = "Returns a paginated list of problems. You can optionally filter by OJ, code, or title. Supports sorting and pagination (requires login)"
    )
    public ResponseEntity<Page<ProblemResponse>> getAllProblems(
            @RequestParam(value = "online_judge", required = false) String onlineJudge,
            @RequestParam(value = "problemCode", required = false) String problemCode,
            @RequestParam(value = "problemTitle", required = false) String problemTitle,
            @SortDefault(
                    sort = "fetchedAt", direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<ProblemResponse> response = problemService.getAllProblems(onlineJudge, problemCode, problemTitle, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reset-filters")
    @Operation(
            summary = "Get all problems without filters",
            description = "Returns a paginated list of problems sorted DESC according to fetched time (requires login)"
    )
    public ResponseEntity<Page<ProblemResponse>> resetFilters(
            @SortDefault(sort = "fetchedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProblemResponse> response = problemService.getAllProblems(null, null, null, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(
            summary = "Update favorite status of a problem",
            description = "Adds or removes a problem from user's favorites (requires login)"
    )
    public ResponseEntity updateFavorite(@Valid @RequestBody FavoriteRequest request) {
        problemService.setFavorite(request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/favorites")
    @Operation(
            summary = "Get all favorite problems",
            description = "Returns a paginated list of favorite problems sorted DESC according to fetched time (requires login)"
    )
    public ResponseEntity<Page<ProblemResponse>> getFavoriteProblems(Pageable pageable) {
        Page<ProblemResponse> response = problemService.getFavorites(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/solved")
    @Operation(
            summary = "Get all solved problems",
            description = "Returns a paginated list of solved problems sorted DESC according to fetched time (requires login)"
    )
    public ResponseEntity<Page<ProblemResponse>> getSolvedProblems(Pageable pageable) {
        Page<ProblemResponse> response = problemService.getSolved(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempted")
    @Operation(
            summary = "Get all attempted problems",
            description = "Returns a paginated list of attempted problems sorted DESC according to fetched time (requires login)"
    )
    public ResponseEntity<Page<ProblemResponse>> getAttemptedProblems(Pageable pageable) {
        Page<ProblemResponse> response = problemService.getAttempted(pageable);
        return ResponseEntity.ok(response);
    }
}
