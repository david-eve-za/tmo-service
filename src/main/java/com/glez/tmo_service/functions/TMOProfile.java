package com.glez.tmo_service.functions;

import lombok.extern.slf4j.Slf4j;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TMOProfile extends TMOAuth {

    public TMOProfile() {
        super();
    }

    public List<String> getProfileList() {
        List<String> hrefs = new ArrayList<>();
        try {
            removeRequestHeader("referer");
            HtmlPage page = getPage("https://lectortmo.com/profile/lists");
            if (isBanned(page)) {
                return hrefs;
            }
            List<HtmlDivision> byXPath = page.getByXPath("//*[@id=\"app\"]/section/main/div/div/div[1]/div[1]/div");
            byXPath.remove(0);

            //Retrieve every href from the list

            for (HtmlDivision div : byXPath) {
                hrefs.add(div.getElementsByTagName("a").get(0).getAttribute("href"));
            }

        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return hrefs;
    }
}
