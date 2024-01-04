package com.glez.tmo_service.functions;

import lombok.extern.slf4j.Slf4j;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TMOBookInformation extends TMOProfileBooks {
    public TMOBookInformation() {
        super();
    }

    public List<Map<String, String>> getBookInfo(String url) {
        List<Map<String, String>> chapters = new ArrayList<>();
        try {
            removeRequestHeader("referer");
            HtmlPage page = getPage(url);
            if (isBanned(page)) {
                return chapters;
            }
            List<HtmlListItem> byXPath = page.getByXPath("//*[@id='chapters']/ul/li");
            byXPath.addAll(page.getByXPath("//*[@id='chapters-collapsed']/li"));

            for (HtmlListItem li : byXPath) {
                Map<String, String> chapter = new HashMap<>();
                chapter.put("url", ((HtmlElement) li.getByXPath("div/div/ul/li[1]/div/div[6]").get(0)).getElementsByTagName("a").get(0).getAttribute("href"));
                chapter.put("title", li.getElementsByTagName("h4").get(0).getTextContent().trim());
                chapters.add(chapter);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return chapters;
    }
}
