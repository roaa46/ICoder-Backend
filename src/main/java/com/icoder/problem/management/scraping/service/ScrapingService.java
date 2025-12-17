package com.icoder.problem.management.scraping.service;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;

public interface ScrapingService {
    ProblemStatementResponse scrapFullStatement(String source, String code);
    ProblemResponse scrapMetaData(String source, String code);
}
