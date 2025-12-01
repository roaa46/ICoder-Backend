package com.icoder.problem.management.service.interfaces;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemService {
    ProblemResponse getProblemMetadata(String source, String code);

    ProblemStatementResponse getProblemStatement(String source, String code);

    Page<ProblemResponse> getAllProblems(String oj, String code, String title, Pageable pageable);

    Page<ProblemResponse> getAttempted(Pageable pageable);

    Page<ProblemResponse> getSolved(Pageable pageable);

    Page<ProblemResponse> getFavorites(Pageable pageable);
}