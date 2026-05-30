package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.codeforces.CodeforcesScraperService;
import org.springframework.stereotype.Component;

@Component
public class GymScraperStrategy extends AbstractCodeforcesScraperStrategy {

    public GymScraperStrategy(CodeforcesScraperService codeforcesScraperService) {
        super(codeforcesScraperService);
    }

    @Override
    public OJudgeType getSupportedJudge() {
        return OJudgeType.GYM;
    }

    @Override
    public ProblemStatementResponse scrapFullStatement(String code) {
        return codeforcesScraperService.scrapProblemStatement(toCodeforcesUrl(code, "gym"));
    }

    @Override
    public ProblemResponse scrapMetaData(String code) {
        return codeforcesScraperService.scrapMetadata(toCodeforcesUrl(code, "gym"));
    }
}