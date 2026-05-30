package com.icoder.problem.management.scraping.service;

import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BrowserService {
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public String fetchHtml(String url) {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"))) {

            Page page = context.newPage();

            page.route("**/*", route -> {
                String type = route.request().resourceType();
                if (List.of("image", "stylesheet", "font", "media").contains(type)) {
                    route.abort();
                } else {
                    route.resume();
                }
            });

            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));

            try {
                page.waitForSelector(".problem-statement", new Page.WaitForSelectorOptions().setTimeout(5000));
            } catch (Exception e) {
                log.warn("Selector .problem-statement not found or timed out, returning content state as is.");
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