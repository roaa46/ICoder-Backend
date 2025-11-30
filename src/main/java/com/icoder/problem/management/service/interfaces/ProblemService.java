package com.icoder.problem.management.service.interfaces;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemService {
    ProblemStatementResponse getProblem(OJudgeType judgeSource, String ProblemCode);
    Page<ProblemResponse> getAllProblems(Pageable pageable);
}
