package com.icoder.problem.management.service.interfaces;

import com.icoder.problem.management.dto.ProblemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemService {
    Page<ProblemResponse> getAllProblems(Pageable pageable);
}
