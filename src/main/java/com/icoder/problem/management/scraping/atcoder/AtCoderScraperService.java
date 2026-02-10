package com.icoder.problem.management.scraping.atcoder;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;

public interface AtCoderScraperService {
    ProblemResponse scrapMetadata(String problemUrl);

    ProblemStatementResponse scrapProblemStatement(String problemUrl);
}