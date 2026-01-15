package com.icoder.problem.management.scraping.codeforces;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.*;
import com.icoder.problem.management.enums.FormatType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CodeforcesScraperServiceImpl implements CodeforcesScraperService {

    @Override
    public ProblemResponse scrapMetadata(String url) {
        try {
            log.info("extract metadata");
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept", "text/html")
                    .header("Connection", "keep-alive")
                    .referrer("https://www.google.com")
                    .timeout(10_000)
                    .get();
            log.info("test extraction");

            Element titleEl = doc.selectFirst(".problem-statement .title");
            String fullTitle = titleEl != null ? titleEl.text() : "";

            String problemCode = extractProblemCodeFromTitle(doc.title());
            String problemTitle = fullTitle;

            Element contestLink = doc.selectFirst("a[href^=/contest/]");

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemTitle(problemTitle)
                    .problemLink(url)
                    .onlineJudge("codeforces")
                    .contestTitle(contestLink != null ? contestLink.text() : "")
                    .contestLink(contestLink != null ? contestLink.absUrl("href") : "")
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("Codeforces metadata failed");
        }
    }

    @Override
    public ProblemStatementResponse scrapProblemStatement(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept", "text/html")
                    .header("Connection", "keep-alive")
                    .referrer("https://www.google.com")
                    .timeout(10_000)
                    .get();

            Element root = doc.selectFirst(".problem-statement");
            if (root == null) throw new ScrapingException("Problem statement not found");

            List<PropertyScrapeDTO> properties = extractProperties(root);
            List<SectionScrapeDTO> sections = extractSections(root);

            return ProblemStatementResponse.builder()
                    .properties(properties)
                    .sections(sections)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("Codeforces statement failed");
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Element root) {
        List<PropertyScrapeDTO> props = new ArrayList<>();
        int index = 1;

        Element time = root.selectFirst(".time-limit");
        Element memory = root.selectFirst(".memory-limit");

        if (time != null) {
            props.add(buildProp("Time Limit",
                    time.text().replace("Time limit per test", "").trim(),
                    index++));
        }

        if (memory != null) {
            props.add(buildProp("Memory Limit",
                    memory.text().replace("Memory limit per test", "").trim(),
                    index++));
        }

        return props;
    }

    private PropertyScrapeDTO buildProp(String title, String content, int index) {
        return PropertyScrapeDTO.builder()
                .title(title)
                .content(content)
                .contentType(FormatType.PLAIN_TEXT.name())
                .orderIndex(index)
                .spoiler(false)
                .build();
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Element root) {
        List<SectionScrapeDTO> sections = new ArrayList<>();
        int sectionIndex = 1;

        for (Element div : root.children()) {

            if (div.hasClass("header") || div.hasClass("footer"))
                continue;

            String title = detectSectionTitle(div);
            if (title == null) continue;

            List<ContentScrapeDTO> contents = extractContents(div);
            sections.add(SectionScrapeDTO.builder()
                    .title(title)
                    .orderIndex(sectionIndex++)
                    .contents(contents)
                    .build());
        }

        return sections;
    }

    private String detectSectionTitle(Element div) {
        if (div.hasClass("input-specification")) return "Input";
        if (div.hasClass("output-specification")) return "Output";
        if (div.hasClass("constraints")) return "Constraints";
        if (div.hasClass("sample-test")) return "Samples";
        if (div.tagName().equals("div")) return "Problem Description";
        return null;
    }

    private List<ContentScrapeDTO> extractContents(Element container) {
        List<ContentScrapeDTO> contents = new ArrayList<>();
        int index = 1;

        for (Node node : container.childNodes()) {

            if (node instanceof Element el) {

                for (Element img : el.select("img")) {
                    img.attr("src", img.absUrl("src"));
                }

                contents.add(ContentScrapeDTO.builder()
                        .content(el.outerHtml())
                        .formatType(FormatType.HTML)
                        .orderIndex(index++)
                        .build());

            } else if (node instanceof TextNode tn && !tn.text().trim().isEmpty()) {

                contents.add(ContentScrapeDTO.builder()
                        .content("<p>" + tn.text().trim() + "</p>")
                        .formatType(FormatType.HTML)
                        .orderIndex(index++)
                        .build());
            }
        }

        return contents;
    }

    private String extractProblemCodeFromTitle(String title) {
        // defensive
        if (title == null || !title.contains("Problem -")) {
            return null;
        }

        return title
                .replace("Problem -", "")
                .replace("- Codeforces", "")
                .trim();
    }

}