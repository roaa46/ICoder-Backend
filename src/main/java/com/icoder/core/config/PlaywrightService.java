package com.icoder.core.config;

import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PlaywrightService {
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        createInstance();
    }

    private synchronized void createInstance() {
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception ignore) {
            }
        }
        log.info("Launching new Browser instance...");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of("--disable-blink-features=AutomationControlled", "--no-sandbox")));
    }

    public <T> T execute(java.util.function.Function<Page, T> action) {
        if (browser == null || !browser.isConnected()) {
            createInstance();
        }

        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080))) {

            Page page = context.newPage();
            blockUnnecessaryResources(page);

            return action.apply(page);
        } catch (Exception e) {
            log.error("Error during browser action: ", e);
            if (e.getMessage() != null && (e.getMessage().contains("Connection closed") || e.getMessage().contains("adopt"))) {
                createInstance();
            }
            throw e;
        }
    }

    private void blockUnnecessaryResources(Page page) {
        page.route("**/*", route -> {
            String type = route.request().resourceType();
            if (List.of("image", "font", "media").contains(type)) {
                route.abort();
            } else {
                route.resume();
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
