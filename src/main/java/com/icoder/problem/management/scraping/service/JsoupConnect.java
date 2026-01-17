package com.icoder.problem.management.scraping.service;

import com.icoder.core.exception.ScrapingException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JsoupConnect {

    private final BrowserService browserService;

    public JsoupConnect(BrowserService browserService) {
        this.browserService = browserService;
    }

    public Document connect(String url) {
        try {
            log.info("Fetching HTML using Playwright for url: {}", url);

            String htmlContent = browserService.fetchHtml(url);

            if (htmlContent == null || htmlContent.isBlank()) {
                throw new ScrapingException("Received empty HTML from browser");
            }

            return Jsoup.parse(htmlContent, url);

        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process page content for url={}", url, e);
            throw new ScrapingException("Failed to connect to remote page via browser", e);
        }
    }
}
