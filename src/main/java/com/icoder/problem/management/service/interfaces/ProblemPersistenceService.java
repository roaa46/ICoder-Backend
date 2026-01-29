package com.icoder.problem.management.service.interfaces;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;

public interface ProblemPersistenceService {
    ProblemResponse saveScrapedMetadata(ProblemResponse response, OJudgeType judgeType);
    ProblemStatementResponse saveFullStatement(ProblemStatementResponse response, OJudgeType judgeType);
}