package com.icoder.problem.management.scraping.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JsoupConnect {
    public Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept", "text/html")
                .header("Connection", "keep-alive")
                .referrer("https://www.google.com")
                .timeout(10_000)
                .get();
    }
}
