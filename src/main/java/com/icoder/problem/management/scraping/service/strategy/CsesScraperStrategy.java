package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.cses.CSESScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsesScraperStrategy implements ScraperStrategy {
    private final CSESScraperService csesScraperService;

    @Override
    public OJudgeType getSupportedJudge() {
        return OJudgeType.CSES;
    }

    @Override
    public ProblemStatementResponse scrapFullStatement(String code) {
        return csesScraperService.scrapProblemStatement(toCsesUrl(code));
    }

    @Override
    public ProblemResponse scrapMetaData(String code) {
        return csesScraperService.scrapMetadata(toCsesUrl(code));
    }

    private String toCsesUrl(String code) {
        return "https://cses.fi/problemset/task/" + code;
    }
}