package com.icoder.problem.management.scraping.cses;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;

public interface CSESScraperService {
    ProblemResponse scrapMetadata(String url);
    ProblemStatementResponse scrapProblemStatement(String url);
}
