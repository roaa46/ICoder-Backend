package com.icoder.problem.management.scraping.service;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;

public interface ScrapingService {
    public ProblemStatementResponse scrapFullStatement(String source, String code);
    public ProblemResponse scrapMetaData(String source, String code);
}
