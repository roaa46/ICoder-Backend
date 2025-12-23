package com.icoder.problem.management.scraping.service;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.cses.CSESScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingServiceImpl implements ScrapingService{
    private final CSESScraperService cses;

    public ProblemStatementResponse scrapFullStatement(String source, String code) {
        try {
            String judge = OJudgeType.fromString(source).name();
            if(judge.equals("CSES")) {
                String problemUrl = "https://cses.fi/problemset/task/" + code;
                return cses.scrapProblemStatement(problemUrl);
            } else if (OJudgeType.fromString(source).equals("CODEFORCES")) {

            } else if (OJudgeType.fromString(source).equals("AT_CODER")) {

            }

        } catch (ScrapingException e) {
            throw new ScrapingException("Failed to scrape full statement of the problem");
        }
        throw new ScrapingException("Failed to scrape full statement of the problem");
    }

    public ProblemResponse scrapMetaData(String source, String code) {
        try {
            log.info("try");
            String judge = OJudgeType.fromString(source).name();
            if(judge.equals("CSES")) {
                log.info("cses");
                String problemUrl = "https://cses.fi/problemset/task/" + code;
                return cses.scrapMetadata(problemUrl);
            } else if (OJudgeType.fromString(source).equals("CODEFORCES")) {

            } else if (OJudgeType.fromString(source).equals("AT_CODER")) {

            }

        } catch (ScrapingException e) {
            throw new ScrapingException("Failed to scrape metadata of the problem");
        }
        throw new ScrapingException("Failed to scrape metadata of the problem");
    }
}
