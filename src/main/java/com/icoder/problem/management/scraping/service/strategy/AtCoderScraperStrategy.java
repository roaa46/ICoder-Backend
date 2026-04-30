package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.atcoder.AtCoderScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AtCoderScraperStrategy implements ScraperStrategy {
    private final AtCoderScraperService atcoderScraperService;

    @Override
    public OJudgeType getSupportedJudge() {
        return OJudgeType.AT_CODER;
    }

    @Override
    public ProblemStatementResponse scrapFullStatement(String code) {
        return atcoderScraperService.scrapProblemStatement(toAtCoderUrl(code));
    }

    @Override
    public ProblemResponse scrapMetaData(String code) {
        return atcoderScraperService.scrapMetadata(toAtCoderUrl(code));
    }

    private String toAtCoderUrl(String code) {
        String contest = code.replaceAll("_.*$", "");
        return "https://atcoder.jp/contests/" + contest + "/tasks/" + code;
    }
}