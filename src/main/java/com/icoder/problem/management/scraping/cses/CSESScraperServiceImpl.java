package com.icoder.problem.management.scraping.cses;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.dto.PropertyScrapeDTO;
import com.icoder.problem.management.dto.SectionScrapeDTO;
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

            String contestTitle = doc.select("div.nav.sidebar h4").text();

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

            // ================= PROPERTIES (TIME / MEMORY / SOURCE) =================
            List<PropertyScrapeDTO> properties = new ArrayList<>();

            Element limits = doc.selectFirst("ul.task-constraints");
            if (limits != null) {
                int index = 1;

                for (Element li : limits.select("li")) {
                    String[] parts = li.text().split(":");
                    if (parts.length == 2) {
                        PropertyScrapeDTO property = new PropertyScrapeDTO();
                        property.setTitle(parts[0].trim());
                        property.setContent(parts[1].trim());
                        property.setContentType(FormatType.PLAIN_TEXT.name());
                        property.setSpoiler(false);
                        property.setOrderIndex(index++);
                        properties.add(property);
                    }
                }

                String contestTitle = doc.select("div.nav.sidebar h4").text();
                properties.add(PropertyScrapeDTO.builder()
                        .title("Source")
                        .content(contestTitle)
                        .orderIndex(index)
                        .contentType(FormatType.PLAIN_TEXT.name())
                        .spoiler(true)
                        .build());
            }


            // ================= SECTIONS =================
            List<SectionScrapeDTO> sections = new ArrayList<>();

            Element content = doc.selectFirst(".content .md");

            // Fix relative image URLs
            for (Element img : content.select("img")) {
                img.attr("src", img.absUrl("src"));
            }

            String currentTitle = "Problem Statement";
            StringBuilder buffer = new StringBuilder();

            int index = 1;

            for (Node node : content.childNodes()) {

                if (node instanceof Element el) {

                    if (el.tagName().equals("h1")) {
                        if (!buffer.isEmpty()) {
                            sections.add(createSection(currentTitle, buffer.toString(), index++));
                            buffer.setLength(0);
                        }
                        currentTitle = el.text();
                    } else {
                        buffer.append(el.outerHtml());
                    }
                } else if (node instanceof TextNode) {
                    String text = ((TextNode) node).getWholeText().trim();
                    if (!text.isEmpty()) {
                        buffer.append("<p>").append(text).append("</p>");
                    }
                }
            }

            if (!buffer.isEmpty()) {
                sections.add(createSection(currentTitle, buffer.toString(), index));
            }

            return ProblemStatementResponse.builder()
                    .sections(sections)
                    .properties(properties)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("Failed to scrape CSES problem");
        }
    }

    private SectionScrapeDTO createSection(String title, String html, int index) {
        return SectionScrapeDTO.builder()
                .title(title)
                .content(html)
                .orderIndex(index)
                .contentType(FormatType.HTML.name())
                .build();
    }
}
