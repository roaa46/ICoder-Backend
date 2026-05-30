package com.icoder.problem.management.scraping.service;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.service.strategy.ScraperStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingServiceImpl implements ScrapingService {

    private final ScraperStrategyFactory strategyFactory;

    @Override
    public ProblemStatementResponse scrapFullStatement(String source, String code) {
        try {
            OJudgeType judge = OJudgeType.fromString(source);
            return strategyFactory.getStrategy(judge).scrapFullStatement(code);
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape full statement for source=" + source + ", code=" + code, e);
        }
    }

    @Override
    public ProblemResponse scrapMetaData(String source, String code) {
        try {
            OJudgeType judge = OJudgeType.fromString(source);
            return strategyFactory.getStrategy(judge).scrapMetaData(code);
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape metadata for source=" + source + ", code=" + code, e);
        }
    }
}
