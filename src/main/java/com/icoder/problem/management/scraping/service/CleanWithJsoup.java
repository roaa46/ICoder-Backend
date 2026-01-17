package com.icoder.problem.management.scraping.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

@Service
public class CleanWithJsoup {
    public String clean(String html) {
        if (html == null || html.isEmpty()) return "";

        Document doc = Jsoup.parseBodyFragment(html);

        doc.select("span.MathJax_Preview, span.MathJax, span.MathJax_Processed").remove();

        for (Element script : doc.select("script[type=math/tex]")) {
            String mathContent = script.data();
            script.replaceWith(new TextNode(mathContent));
        }

        return doc.body().html();
    }
}
