package com.icoder.problem.management.controller;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.service.implementation.ProblemServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemServiceImpl problemService;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @GetMapping("/{judge_type}/{problem_code}")
    public ResponseEntity<ProblemStatementResponse> getProblem(@PathVariable("judge_type") OJudgeType source,
                                                               @PathVariable("problem_code") String code) {

        return ResponseEntity.ok(problemService.getProblem(source, code));
    }

    @GetMapping
    public ResponseEntity<Page<ProblemResponse>> getAllProblems(
            @SortDefault(
                    sort = "fetchedAt", direction = Sort.Direction.DESC
            )
            Pageable pageable) {
        return ResponseEntity.ok(problemService.getAllProblems(pageable));
    }
}
