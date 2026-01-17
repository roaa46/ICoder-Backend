package com.icoder.problem.management.scraping.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrowserService {
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    public String fetchHtml(String url) {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"))) {

            Page page = context.newPage();

            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));

            try {
                page.waitForSelector(".problem-statement", new Page.WaitForSelectorOptions().setTimeout(5000));
            } catch (Exception e) {
                log.warn("Selector .problem-statement not found, returning raw content anyway.");
            }

            return page.content();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
