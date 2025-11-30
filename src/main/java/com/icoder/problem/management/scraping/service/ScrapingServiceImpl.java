package com.icoder.problem.management.scraping.service;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import org.springframework.stereotype.Service;

@Service
public class ScrapingServiceImpl implements ScrapingService{
    /// remember to convert enums to string and vice versa (enums are upper case, strings are lower case)
    public ProblemStatementResponse scrapFullStatement(String source, String code) {
        return new ProblemStatementResponse();
    }

    public ProblemResponse scrapMetaData(String source, String code) {
        return new ProblemResponse();
    }
}
