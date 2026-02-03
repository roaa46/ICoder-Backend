package com.icoder.problem.management.scraping.service;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.atcoder.AtCoderScraperService;
import com.icoder.problem.management.scraping.codeforces.CodeforcesScraperService;
import com.icoder.problem.management.scraping.cses.CSESScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingServiceImpl implements ScrapingService {
    private final CSESScraperService cses;
    private final AtCoderScraperService atcoder;
    private final CodeforcesScraperService codeforces;

    public ProblemStatementResponse scrapFullStatement(String source, String code) {
        try {
            OJudgeType judge = OJudgeType.fromString(source);
            String problemUrl;

            switch (judge) {
                case CSES -> {
                    problemUrl = toCsesUrl(code);
                    log.info(problemUrl);
                    return cses.scrapProblemStatement(problemUrl);
                }
                case CODEFORCES -> {
                    problemUrl = toCodeforcesUrl(code, "contest");
                    log.info(problemUrl);
                    return codeforces.scrapProblemStatement(problemUrl);
                }
                case AT_CODER -> {
                    problemUrl = toAtCoderUrl(code);
                    log.info(problemUrl);
                    return atcoder.scrapProblemStatement(problemUrl);
                }
                case GYM -> {
                    problemUrl = toCodeforcesUrl(code, "gym");
                    log.info(problemUrl);
                    return codeforces.scrapProblemStatement(problemUrl);
                }
                default -> throw new ScrapingException("Unsupported judge: " + source);
            }
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape full statement for source=" + source + ", code=" + code, e);
        }
    }

    public ProblemResponse scrapMetaData(String source, String code) {
        try {
            OJudgeType judge = OJudgeType.fromString(source);
            String problemUrl;

            switch (judge) {
                case CSES -> {
                    problemUrl = toCsesUrl(code);
                    log.info(problemUrl);
                    return cses.scrapMetadata(problemUrl);
                }
                case CODEFORCES -> {
                    problemUrl = toCodeforcesUrl(code, "contest");
                    log.info(problemUrl);
                    return codeforces.scrapMetadata(problemUrl);
                }
                case AT_CODER -> {
                    problemUrl = toAtCoderUrl(code);
                    log.info(problemUrl);
                    return atcoder.scrapMetadata(problemUrl);
                }
                case GYM -> {
                    problemUrl = toCodeforcesUrl(code, "gym");
                    log.info(problemUrl);
                    return codeforces.scrapMetadata(problemUrl);
                }
                default -> throw new ScrapingException("Unsupported judge: " + source);
            }
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape metadata for source=" + source + ", code=" + code, e);
        }
    }

    private String toCsesUrl(String code) {
        return "https://cses.fi/problemset/task/" + code;
    }

    private String toCodeforcesUrl(String code, String source) {
        int i = 0;
        while (i < code.length() && Character.isDigit(code.charAt(i))) i++;
        int id = Integer.parseInt(code.substring(0, i));
        String order = code.substring(i);
        return "https://codeforces.com/" + source + "/" + id + "/problem/" + order;
    }

    private String toAtCoderUrl(String code) {
        String contest = code.replaceAll("_.*$", "");
        return "https://atcoder.jp/contests/" + contest + "/tasks/" + code;
    }

}