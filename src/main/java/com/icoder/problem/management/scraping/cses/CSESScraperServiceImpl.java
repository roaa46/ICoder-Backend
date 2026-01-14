package com.icoder.problem.management.scraping.cses;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.*;
import com.icoder.problem.management.enums.FormatType;
import com.icoder.problem.management.enums.OJudgeType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CSESScraperServiceImpl implements CSESScraperService {
    @Override
    public ProblemResponse scrapMetadata(String problemUrl) {
        try {
            Document doc = Jsoup.connect(problemUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            String problemCode = problemUrl.replaceAll(".*/", "");
            String problemTitle = doc.selectFirst(".navigation h1").text();

            Element sidebar = doc.selectFirst("div.nav.sidebar");
            String contestTitle = "";

            if (sidebar != null) {
                Element firstH4 = sidebar.selectFirst("h4");
                if (firstH4 != null) {
                    contestTitle = firstH4.text();
                }
            }

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemLink(problemUrl)
                    .onlineJudge(OJudgeType.CSES.name())
                    .problemTitle(problemTitle)
                    .contestTitle(contestTitle)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape CSES problem");
        }
    }

    @Override
    public ProblemStatementResponse scrapProblemStatement(String problemUrl) {
        try {
            Document doc = Jsoup.connect(problemUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            List<PropertyScrapeDTO> properties = extractProperties(doc);
            List<SectionScrapeDTO> sections = extractSections(doc);


            return ProblemStatementResponse.builder()
                    .sections(sections)
                    .properties(properties)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape CSES problem");
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Document doc) {
        List<PropertyScrapeDTO> properties = new ArrayList<>();

        Element limits = doc.selectFirst("ul.task-constraints");
        if (limits != null) {
            int index = 1;
            for (Element li : limits.select("li")) {
                String[] parts = li.text().split(":");
                if (parts.length == 2) {
                    properties.add(PropertyScrapeDTO.builder()
                            .title(parts[0].trim())
                            .content(parts[1].trim())
                            .contentType(FormatType.PLAIN_TEXT.name())
                            .orderIndex(index++)
                            .spoiler(false)
                            .build());
                }
            }

            Element sidebar = doc.selectFirst("div.nav.sidebar");
            String contestTitle = "";

            if (sidebar != null) {
                Element firstH4 = sidebar.selectFirst("h4");
                if (firstH4 != null) {
                    contestTitle = firstH4.text();
                }
            }

            properties.add(PropertyScrapeDTO.builder()
                    .title("Source")
                    .content(contestTitle)
                    .contentType(FormatType.PLAIN_TEXT.name())
                    .orderIndex(index)
                    .spoiler(true)
                    .build());
        }
        return properties;
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Document doc) {
        List<SectionScrapeDTO> sections = new ArrayList<>();

        Element content = doc.selectFirst(".content .md");

        // fix relative images
        for (Element img : content.select("img")) {
            img.attr("src", img.absUrl("src"));
        }

        int sectionIndex = 1;
        int contentIndex = 1;

        String currentTitle = null;
        List<ContentScrapeDTO> currentContents = new ArrayList<>();

        for (Node node : content.childNodes()) {

            if (node instanceof Element el && el.tagName().equals("h1")) {

                // close a previous section
                if (!currentContents.isEmpty()) {
                    sections.add(createSection(
                            currentTitle,
                            sectionIndex++,
                            currentContents
                    ));
                    currentContents = new ArrayList<>();
                    contentIndex = 1;
                }

                currentTitle = el.text();
                continue;
            }

            if (node instanceof Element el) {
                currentContents.add(createContent(el.outerHtml(), contentIndex++));
            } else if (node instanceof TextNode tn) {
                String text = tn.getWholeText().trim();
                if (!text.isEmpty()) {
                    currentContents.add(createContent(
                            "<p>" + text + "</p>",
                            contentIndex++
                    ));
                }
            }
        }

        // last section
        if (!currentContents.isEmpty()) {
            sections.add(createSection(
                    currentTitle,
                    sectionIndex,
                    currentContents
            ));
        }

        return sections;
    }

    private SectionScrapeDTO createSection(
            String title,
            int orderIndex,
            List<ContentScrapeDTO> contents
    ) {
        return SectionScrapeDTO.builder()
                .title(title)
                .orderIndex(orderIndex)
                .contents(contents)
                .build();
    }

    private ContentScrapeDTO createContent(String html, int index) {
        return ContentScrapeDTO.builder()
                .content(html)
                .formatType(FormatType.HTML)
                .orderIndex(index)
                .build();
    }

}