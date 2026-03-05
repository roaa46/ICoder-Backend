package com.icoder.problem.management.scraping.atcoder;

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
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AtCoderScraperServiceImpl implements AtCoderScraperService {
    private final CleanWithJsoup cleanWithJsoup;
    private final ProblemUtils problemUtils;

    public AtCoderScraperServiceImpl(CleanWithJsoup cleanWithJsoup, ProblemUtils problemUtils) {
        this.cleanWithJsoup = cleanWithJsoup;
        this.problemUtils = problemUtils;
    }

    @Override
    public ProblemResponse scrapMetadata(String url) {
        try {
            log.info("Extract metadata of: [{}]", url);
            Document doc = problemUtils.getDocument(url);

            String problemCode = url.substring(url.lastIndexOf("/") + 1);

            Element titleEl = doc.selectFirst("span.h2");
            if (titleEl != null) {
                titleEl.select("a").remove();
            }
            String problemTitle = titleEl != null ? titleEl.text().trim() : problemCode;

            Element contestEl = doc.selectFirst("a[href^=/contests/]");
            String contestTitle = contestEl != null ? contestEl.text() : "";
            String contestLink = contestEl != null ? contestEl.absUrl("href") : "";

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemLink(url)
                    .onlineJudge(OJudgeType.AT_CODER)
                    .problemTitle(problemTitle)
                    .contestTitle(contestTitle)
                    .contestLink(contestLink)
                    .build();

        } catch (Exception e) {
            log.error("AtCoder metadata scraping failed: {}", url, e);
            throw new ScrapingException("AtCoder metadata failed");
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
                    .properties(properties)
                    .sections(sections)
                    .build();

        } catch (Exception e) {
            log.error("AtCoder statement scraping failed: {}", url, e);
            throw new ScrapingException("AtCoder statement failed");
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Document doc) {
        List<PropertyScrapeDTO> list = new ArrayList<>();
        int index = 1;

        Element limits = doc.selectFirst("p:contains(Time Limit)");
        if (limits != null) {
            String[] parts = limits.text().split("/");
            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    list.add(buildProp(kv[0].trim(), kv[1].trim(), index++, false));
                }
            }
        }

        Element contest = doc.selectFirst("a[href^=/contests/]");
        if (contest != null) {
            list.add(buildProp("Source", contest.text(), index++, true));
        }

        return list;
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Document doc) {
        List<SectionScrapeDTO> sections = new ArrayList<>();

        Element taskStatement = doc.selectFirst("#task-statement");
        if (taskStatement == null) return sections;

        // AtCoder contains sections in different languages, we prefer English
        Elements langElements = taskStatement.select(".lang-en, .lang[data-lang=en]");
        Element lang = langElements.isEmpty() ? taskStatement.selectFirst(".lang-ja, .lang[data-lang=ja]") : langElements.first();
        if (lang == null) return sections;

        Elements parts = lang.select(".part");
        int sectionIndex = 1;

        for (Element part : parts) {
            Element h3 = part.selectFirst("h3");
            if (h3 == null) continue;

            String title = h3.text().trim();

            sections.add(SectionScrapeDTO.builder()
                    .title(title)
                    .orderIndex(sectionIndex++)
                    .contents(extractContents(part))
                    .build());
        }

        return sections;
    }

    private List<ContentScrapeDTO> extractContents(Element sectionContainer) {
        List<ContentScrapeDTO> contents = new ArrayList<>();
        int contentIndex = 1;

        for (Node node : sectionContainer.childNodes()) {
            if (node instanceof Element el && el.tagName().equals("h3")) {
                continue;
            }

            if (node instanceof Element el) {
                el.select("img").forEach(img -> img.attr("src", img.absUrl("src")));

                String cleanedHtml = cleanWithJsoup.clean(el.outerHtml());
                if (!cleanedHtml.isEmpty()) {
                    contents.add(buildContent(cleanedHtml, contentIndex++));
                }
            } else if (node instanceof TextNode tn && !tn.text().trim().isEmpty()) {
                contents.add(buildContent("<p>" + tn.text().trim() + "</p>", contentIndex++));
            }
        }
        return contents;
    }

    // ================= UTILS =================
    private PropertyScrapeDTO buildProp(String title, String content, int order, boolean spoiler) {
        return PropertyScrapeDTO.builder()
                .title(title)
                .content(content)
                .contentType(FormatType.PLAIN_TEXT.name())
                .orderIndex(order)
                .spoiler(spoiler)
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