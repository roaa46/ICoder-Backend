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
            log.debug("Attempting direct Jsoup connection for: {}", url);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();
            if (isValidContent(doc)) {
                log.info("Successfully fetched via Jsoup (Direct): {}", url);
                return doc;
            } else {
                log.warn("Jsoup content likely incomplete (JS required). Falling back to Playwright.");
            }
        } catch (Exception e) {
            log.warn("Jsoup direct connection failed or timed out. Falling back to Playwright. Error: {}", e.getMessage());
        }

        return connectWithBrowser(url);
    }

    private Document connectWithBrowser(String url) {
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
            log.error("Failed to process page content via browser for url={}", url, e);
            throw new ScrapingException("Failed to connect to remote page", e);
        }
    }

    private boolean isValidContent(Document doc) {
        return doc.body().text().length() > 200;
    }
}