package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;

public interface ScraperStrategy {

    /**
     * Identifies which Online Judge this strategy supports.
     */
    OJudgeType getSupportedJudge();

    ProblemStatementResponse scrapFullStatement(String code);

    ProblemResponse scrapMetaData(String code);
}