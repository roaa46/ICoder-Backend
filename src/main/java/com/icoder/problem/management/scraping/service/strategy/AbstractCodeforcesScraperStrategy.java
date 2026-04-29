package com.icoder.problem.management.scraping.service.strategy;

import com.icoder.problem.management.scraping.codeforces.CodeforcesScraperService;

public abstract class AbstractCodeforcesScraperStrategy implements ScraperStrategy {
    protected final CodeforcesScraperService codeforcesScraperService;

    protected AbstractCodeforcesScraperStrategy(CodeforcesScraperService codeforcesScraperService) {
        this.codeforcesScraperService = codeforcesScraperService;
    }

    protected String toCodeforcesUrl(String code, String source) {
        int i = 0;
        while (i < code.length() && Character.isDigit(code.charAt(i))) i++;
        int id = Integer.parseInt(code.substring(0, i));
        String order = code.substring(i);
        return "https://codeforces.com/" + source + "/" + id + "/problem/" + order;
    }
}