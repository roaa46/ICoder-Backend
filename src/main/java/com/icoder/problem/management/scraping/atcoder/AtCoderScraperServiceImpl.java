package com.icoder.problem.management.scraping.atcoder;

import com.icoder.core.exception.ScrapingException;
import com.icoder.problem.management.dto.*;
import com.icoder.problem.management.enums.FormatType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AtCoderScraperServiceImpl implements AtCoderScraperService{

    public ProblemResponse scrapMetadata(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();
            log.info("test extraction");

            String problemCode = url.substring(url.lastIndexOf("/") + 1);

            Element titleEl = doc.selectFirst("span.h2");
            if (titleEl != null) {
                titleEl.select("a").remove();
            }
            String problemTitle = titleEl != null ? titleEl.text() : problemCode;

            Element contestEl = doc.selectFirst("a[href^=/contests/]");
            String contestTitle = contestEl != null ? contestEl.text() : "";
            String contestLink = contestEl != null ? contestEl.absUrl("href") : "";

            return ProblemResponse.builder()
                    .problemCode(problemCode)
                    .problemLink(url)
                    .onlineJudge("atcoder")
                    .problemTitle(problemTitle)
                    .contestTitle(contestTitle)
                    .contestLink(contestLink)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("AtCoder metadata failed");
        }
    }

    @Override
    public ProblemStatementResponse scrapProblemStatement(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            List<PropertyScrapeDTO> properties = extractProperties(doc);
            List<SectionScrapeDTO> sections = extractSections(doc);

            return ProblemStatementResponse.builder()
                    .properties(properties)
                    .sections(sections)
                    .build();

        } catch (Exception e) {
            throw new ScrapingException("AtCoder statement failed");
        }
    }

    // ================= PROPERTIES =================
    private List<PropertyScrapeDTO> extractProperties(Document doc) {
        List<PropertyScrapeDTO> list = new ArrayList<>();

        Element limits = doc.selectFirst("p:contains(Time Limit)");
        if (limits != null) {
            String[] parts = limits.text().split("/");
            int index = 1;

            for (String part : parts) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    list.add(PropertyScrapeDTO.builder()
                            .title(kv[0].trim())
                            .content(kv[1].trim())
                            .contentType(FormatType.PLAIN_TEXT.name())
                            .orderIndex(index++)
                            .spoiler(false)
                            .build());
                }
            }
        }

        Element contest = doc.selectFirst("a[href^=/contests/]");
        if (contest != null) {
            list.add(PropertyScrapeDTO.builder()
                    .title("Source")
                    .content(contest.text())
                    .contentType(FormatType.PLAIN_TEXT.name())
                    .orderIndex(list.size() + 1)
                    .spoiler(true)
                    .build());
        }

        return list;
    }

    // ================= SECTIONS =================
    private List<SectionScrapeDTO> extractSections(Document doc) {
        List<SectionScrapeDTO> sections = new ArrayList<>();

        Element taskStatement = doc.selectFirst("#task-statement");
        if (taskStatement == null) return sections;

        Elements langElements = taskStatement.select(".lang-en, .lang[data-lang=en]");
        Element lang = langElements.isEmpty() ? taskStatement.selectFirst(".lang-ja, .lang[data-lang=ja]") : langElements.first();
        if (lang == null) return sections;

        Elements parts = lang.select(".part");
        int sectionIndex = 1;

        for (Element part : parts) {
            Element h3 = part.selectFirst("h3");
            if (h3 == null) continue;

            String title = h3.text().trim();
            List<ContentScrapeDTO> contents = new ArrayList<>();
            int contentIndex = 1;

            for (Element child : part.children()) {
                if (child.tagName().equals("h3")) continue;

                // fix images
                for (Element img : child.select("img")) {
                    img.attr("src", img.absUrl("src"));
                }

                contents.add(ContentScrapeDTO.builder()
                        .content(child.outerHtml())
                        .formatType(FormatType.HTML)
                        .orderIndex(contentIndex++)
                        .build());
            }

            sections.add(SectionScrapeDTO.builder()
                    .title(title)
                    .orderIndex(sectionIndex++)
                    .contents(contents)
                    .build());
        }

        return sections;
    }
}