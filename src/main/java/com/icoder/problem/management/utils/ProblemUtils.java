package com.icoder.problem.management.utils;

import com.icoder.core.exception.ScrapingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProblemUtils {
    public Document getDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .timeout(10000)
                    .execute()
                    .parse();
        } catch (HttpStatusException e) {
            log.error("Problem not found: status={}, url={}", e.getStatusCode(), url);
            throw new ScrapingException("Problem not found (404) or online judge error.");
        } catch (Exception e) {
            log.error("Failed to connect: {}", url, e);
            throw new ScrapingException("Connection to online judge failed.");
        }
    }
}
