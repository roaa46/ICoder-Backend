package com.icoder.problem.management.scraping.cses;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.*;
import com.icoder.problem.management.enums.FormatType;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.scraping.service.CleanWithJsoup;
import com.icoder.problem.management.utils.ProblemUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CSESScraperServiceImpl implements CSESScraperService {
    private final CleanWithJsoup cleanWithJsoup;
    private final ProblemUtils problemUtils;

    public CSESScraperServiceImpl(CleanWithJsoup cleanWithJsoup, ProblemUtils problemUtils) {
        this.cleanWithJsoup = cleanWithJsoup;
        this.problemUtils = problemUtils;
    }

    @Override
    public ProblemResponse scrapMetadata(String url) {
        try {
            log.info("Extract metadata of: [{}]", url);
            Document doc = problemUtils.getDocument(url);

            String problemCode = url.replaceAll(".*/", "");
            Element titleEl = doc.selectFirst(".navigation h1");
            String problemTitle = titleEl != null ? titleEl.text() : problemCode;

            Element sidebar = doc.selectFirst("div.nav.sidebar");
            String contestTitle = (sidebar != null && sidebar.selectFirst("h4") != null)
                    ? sidebar.selectFirst("h4").text() : "CSES Problem Set";

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemLink(url)
                    .onlineJudge(OJudgeType.CSES)
                    .problemTitle(problemTitle)
                    .contestTitle(contestTitle)
                    .build();

        } catch (Exception e) {
            log.error("CSES metadata scraping failed", e);
            throw new ScrapingException("Failed to scrape CSES problem metadata");
        }
    }

    @Override
    public ProblemStatementResponse scrapProblemStatement(String url) {
        try {
            log.info("Extract statement of: [{}]", url);
            Document doc = problemUtils.getDocument(url);

            List<PropertyScrapeDTO> properties = extractProperties(doc);
            List<SectionScrapeDTO> sections = extractSections(doc);

            return ProblemStatementResponse.builder()
                    .sections(sections)
                    .properties(properties)
                    .build();

        } catch (Exception e) {
            log.error("CSES statement scraping failed", e);
            throw new ScrapingException("Failed to scrape CSES problem statement");
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Document doc) {
        List<PropertyScrapeDTO> properties = new ArrayList<>();
        int index = 1;

        Element limits = doc.selectFirst("ul.task-constraints");
        if (limits != null) {
            for (Element li : limits.select("li")) {
                String text = li.text();
                if (text.contains(":")) {
                    String[] parts = text.split(":", 2);
                    properties.add(buildProperty(parts[0].trim(), parts[1].trim(), index++, false));
                }
            }
        }

        Element sidebar = doc.selectFirst("div.nav.sidebar");
        if (sidebar != null && sidebar.selectFirst("h4") != null) {
            properties.add(buildProperty("Source", sidebar.selectFirst("h4").text(), index, true));
        }

        return properties;
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Document doc) {
        List<SectionScrapeDTO> sections = new ArrayList<>();
        Element root = doc.selectFirst(".content .md");
        if (root == null) return sections;

        // Fix images
        root.select("img").forEach(img -> img.attr("src", img.absUrl("src")));

        int sectionIndex = 1;
        int contentIndex = 1;

        String currentTitle = "Description";
        List<ContentScrapeDTO> currentContents = new ArrayList<>();

        for (Node node : root.childNodes()) {
            if (node instanceof Element el && el.tagName().equals("h1")) {

                if (!currentContents.isEmpty()) {
                    sections.add(buildSection(currentTitle, sectionIndex++, currentContents));
                    currentContents = new ArrayList<>();
                    contentIndex = 1;
                }

                currentTitle = el.text().trim();
                continue;
            }

            if (node instanceof Element el) {
                String cleanedHtml = cleanWithJsoup.clean(el.outerHtml());
                if (!cleanedHtml.isEmpty()) {
                    currentContents.add(buildContent(cleanedHtml, contentIndex++));
                }
            } else if (node instanceof TextNode tn) {
                String text = tn.getWholeText().trim();
                if (!text.isEmpty()) {
                    currentContents.add(buildContent("<p>" + text + "</p>", contentIndex++));
                }
            }
        }

        if (!currentContents.isEmpty()) {
            sections.add(buildSection(currentTitle, sectionIndex, currentContents));
        }

        return sections;
    }

    // ================= HELPERS =================
    private PropertyScrapeDTO buildProperty(String title, String content, int order, boolean spoiler) {
        return PropertyScrapeDTO.builder()
                .title(title)
                .content(content)
                .contentType(FormatType.PLAIN_TEXT.name())
                .orderIndex(order)
                .spoiler(spoiler)
                .build();
    }

    private SectionScrapeDTO buildSection(String title, int order, List<ContentScrapeDTO> contents) {
        return SectionScrapeDTO.builder()
                .title(title)
                .orderIndex(order)
                .contents(contents)
                .build();
    }

    private ContentScrapeDTO buildContent(String html, int order) {
        return ContentScrapeDTO.builder()
                .content(html)
                .formatType(FormatType.HTML)
                .orderIndex(order)
                .build();
    }
}