package com.icoder.problem.management.scraping.service;

import com.icoder.core.exception.ScrapingException;
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

    private final BrowserService browserService;
    private final HttpClient httpClient;

    public JsoupConnect(BrowserService browserService, HttpClient httpClient) {
        this.browserService = browserService;
        this.httpClient = httpClient;
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