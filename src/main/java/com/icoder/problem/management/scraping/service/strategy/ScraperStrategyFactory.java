package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.enums.OJudgeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ScraperStrategyFactory {
    private final Map<OJudgeType, ScraperStrategy> strategies;

    public ScraperStrategyFactory(List<ScraperStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ScraperStrategy::getSupportedJudge, Function.identity()));
    }

    public ScraperStrategy getStrategy(OJudgeType judgeType) {
        ScraperStrategy strategy = strategies.get(judgeType);
        if (strategy == null) {
            throw new ScrapingException("Unsupported judge: " + judgeType);
        }
        return strategy;
    }
}