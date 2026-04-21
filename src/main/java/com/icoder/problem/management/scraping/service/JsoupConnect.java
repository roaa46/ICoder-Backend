package com.icoder.problem.management.scraping.service;

import com.icoder.core.config.PlaywrightService;
import com.icoder.core.exception.ScrapingException;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class JsoupConnect {

    private final PlaywrightService playwrightService;
    private final HttpClient httpClient;

    public JsoupConnect(HttpClient httpClient, PlaywrightService playwrightService) {
        this.httpClient = httpClient;
        this.playwrightService = playwrightService;
    }

    public Document connect(String url) {
        try {
            log.debug("Fetching HTML via HttpClient: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 ...")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Document doc = Jsoup.parse(response.body(), url);
                if (isValidContent(doc)) {
                    log.info("Successfully fetched via HttpClient: {}", url);
                    return doc;
                }
            }

            log.warn("HttpClient returned status {} or invalid content. Falling back to Playwright.", response.statusCode());

        } catch (Exception e) {
            log.warn("HttpClient failed for {}. Error: {}", url, e.getMessage());
        }

        return connectWithBrowser(url);
    }

    private Document connectWithBrowser(String url) {
        try {
            log.info("Fetching HTML using Playwright for url: {}", url);
            String htmlContent = playwrightService.execute(page -> {
                page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                try {
                    page.waitForSelector(".problem-statement", new Page.WaitForSelectorOptions().setTimeout(10000));
                } catch (Exception e) {
                    log.warn("Selector .problem-statement not found, checking if page redirected or blocked");
                }
                return page.content();
            });

            if (htmlContent == null || htmlContent.contains("Redirecting...")) {
                throw new ScrapingException("Cloudflare or Redirect detected");
            }
            return Jsoup.parse(htmlContent, url);
        } catch (Exception e) {
            log.error("Failed to process page content via browser: {}", e.getMessage());
            throw new ScrapingException("Failed to connect to remote page", e);
        }
    }

    private boolean isValidContent(Document doc) {
        return doc.body().text().length() > 200;
    }
}