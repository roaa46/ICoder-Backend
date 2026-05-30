package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.codeforces.CodeforcesScraperService;
import org.springframework.stereotype.Component;

@Component
public class CodeforcesScraperStrategy extends AbstractCodeforcesScraperStrategy {

    public CodeforcesScraperStrategy(CodeforcesScraperService codeforcesScraperService) {
        super(codeforcesScraperService);
    }

    @Override
    public OJudgeType getSupportedJudge() {
        return OJudgeType.CODEFORCES;
    }

    @Override
    public ProblemStatementResponse scrapFullStatement(String code) {
        return codeforcesScraperService.scrapProblemStatement(toCodeforcesUrl(code, "contest"));
    }

    @Override
    public ProblemResponse scrapMetaData(String code) {
        return codeforcesScraperService.scrapMetadata(toCodeforcesUrl(code, "contest"));
    }
}