package com.icoder.problem.management.scraping.codeforces;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.*;
import com.icoder.problem.management.enums.FormatType;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.service.CleanWithJsoup;
import com.icoder.problem.management.scraping.service.JsoupConnect;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CodeforcesScraperServiceImpl implements CodeforcesScraperService {

    private final JsoupConnect jsoupConnect;
    private final CleanWithJsoup cleanWithJsoup;

    public CodeforcesScraperServiceImpl(JsoupConnect jsoupConnect, CleanWithJsoup cleanWithJsoup) {
        this.jsoupConnect = jsoupConnect;
        this.cleanWithJsoup = cleanWithJsoup;
    }

    // ================= METADATA =================
    @Override
    public ProblemResponse scrapMetadata(String url) {
        try {
            log.info("Extract metadata of: [{}]", url);
            Document doc = jsoupConnect.connect(url);

            Element titleEl = doc.selectFirst(".problem-statement .title");
            if (titleEl == null) {
                throw new ScrapingException("Problem title not found");
            }

            String fullTitle = titleEl.text();
            String problemCode = extractProblemCodeFromUrl(url);

//            Element contestEl = doc.selectFirst(".problem-statement .header a");
            Element contestLink = doc.selectFirst("a[href^=/contest/]");

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemTitle(fullTitle)
                    .problemLink(url)
                    .onlineJudge(OJudgeType.CODEFORCES)
                    .contestTitle(contestLink != null ? contestLink.text() : "Problemset")
                    .contestLink(contestLink != null ? contestLink.absUrl("href") : "")
                    .build();

        } catch (ScrapingException e) {
            throw e;

        } catch (Exception e) {
            log.error("Codeforces metadata scraping failed: {}", url, e);
            throw new ScrapingException("Failed to scrape metadata of the problem", e);
        }
    }

    // ================= STATEMENT =================
    @Override
    public ProblemStatementResponse scrapProblemStatement(String url) {
        try {
            log.info("Extract statement of: [{}]", url);
            Document doc = jsoupConnect.connect(url);

            Element root = doc.selectFirst(".problem-statement");
            if (root == null) {
                throw new ScrapingException("Problem statement not found");
            }

            root.select("img").forEach(img -> img.attr("src", img.absUrl("src")));

            List<PropertyScrapeDTO> properties = extractProperties(doc, root);
            List<SectionScrapeDTO> sections = extractSections(root);

            return ProblemStatementResponse.builder()
                    .properties(properties)
                    .sections(sections)
                    .build();

        } catch (ScrapingException e) {
            throw e;

        } catch (Exception e) {
            log.error("Codeforces statement scraping failed: {}", url, e);
            throw new ScrapingException("Failed to scrape problem statement", e);
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Document doc, Element root) {
        try {
            List<PropertyScrapeDTO> props = new ArrayList<>();
            int index = 1;

            Element contestLink = doc.selectFirst("a[href^=/contest/]");

            props.add(buildProp(
                    "Source",
                    contestLink != null ? contestLink.text().trim() : "",
                    index++,
                    true
            ));

            Element time = root.selectFirst(".time-limit");
            Element memory = root.selectFirst(".memory-limit");

            if (time != null) {
                props.add(buildProp("Time Limit",
                        time.text().replace("Time limit per test", "").trim(),
                        index++, false));
            }

            if (memory != null) {
                props.add(buildProp("Memory Limit",
                        memory.text().replace("Memory limit per test", "").trim(),
                        index++, false));
            }

            Elements tags = doc.select(".tag-box");
            List<String> tagNames = new ArrayList<>();
            String difficulty = null;

            for (Element tag : tags) {
                String text = tag.text().trim();
                if (text.startsWith("*")) {
                    difficulty = text.substring(1);
                } else {
                    tagNames.add(text);
                }
            }

            if (!tagNames.isEmpty()) {
                props.add(buildProp("Tags", String.join(", ", tagNames), index++, true));
            }

            if (difficulty != null) {
                props.add(buildProp("Difficulty", difficulty, index++, true));
            }

            return props;

        } catch (Exception e) {
            throw new ScrapingException("Failed to extract properties", e);
        }
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Element root) {
        try {
            List<SectionScrapeDTO> sections = new ArrayList<>();
            int sectionIndex = 1;

            for (Element child : root.children()) {
                if (child.hasClass("header") || child.hasClass("footer")) continue;

                String title = detectSectionTitle(child);
                if (title == null) continue;

                sections.add(SectionScrapeDTO.builder()
                        .title(title)
                        .orderIndex(sectionIndex++)
                        .contents(extractContents(child))
                        .build());
            }

            return sections;

        } catch (Exception e) {
            throw new ScrapingException("Failed to extract sections", e);
        }
    }

    private String detectSectionTitle(Element el) {
        if (el.hasClass("input-specification")) return "Input";
        if (el.hasClass("output-specification")) return "Output";
        if (el.hasClass("sample-test")) return "Samples";
        if (el.tagName().equals("div")) return "Problem Description";
        return null;
    }

    private List<ContentScrapeDTO> extractContents(Element container) {
        try {
            List<ContentScrapeDTO> contents = new ArrayList<>();
            int index = 1;

            for (Node node : container.childNodes()) {
                if (node instanceof Element el) {
                    String cleanedHtml = cleanWithJsoup.clean(el.outerHtml());

                    contents.add(ContentScrapeDTO.builder()
                            .content(cleanedHtml)
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

        } catch (Exception e) {
            throw new ScrapingException("Failed to extract section contents", e);
        }
    }

    // ================= UTIL =================
    private PropertyScrapeDTO buildProp(
            String title,
            String content,
            int order,
            boolean spoiler
    ) {
        return PropertyScrapeDTO.builder()
                .title(title)
                .content(content)
                .contentType(FormatType.PLAIN_TEXT.name())
                .orderIndex(order)
                .spoiler(spoiler)
                .build();
    }

    private String extractProblemCodeFromUrl(String url) {
        try {
            String[] parts = url.split("/");

            String contestId = parts[4];

            String index = parts[6];

            return contestId + index;
        } catch (Exception e) {
            log.error("Failed to extract code from URL: {}", url);
            return null;
        }
    }
}
