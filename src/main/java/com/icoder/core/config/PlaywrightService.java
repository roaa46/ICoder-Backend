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
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public <T> T execute(java.util.function.Function<Page, T> action) {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 ..."))) {

            Page page = context.newPage();
            blockUnnecessaryResources(page);

            return action.apply(page);
        } catch (Exception e) {
            log.error("Error during browser action: ", e);
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
