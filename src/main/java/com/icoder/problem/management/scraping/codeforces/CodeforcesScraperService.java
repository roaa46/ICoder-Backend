package com.icoder.problem.management.scraping.codeforces;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;

public interface CodeforcesScraperService {
    ProblemResponse scrapMetadata(String url);

    ProblemStatementResponse scrapProblemStatement(String url);
}
