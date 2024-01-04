package com.glez.tmo_service.functions;

import com.glez.tmo_service.base.BrowserBase;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.parser.HTMLParser;
import org.htmlunit.html.parser.neko.HtmlUnitNekoHtmlParser;

@Slf4j
public class TMOAuth extends BrowserBase {

    public TMOAuth() {
        super();
    }

    public void login() {
        try {
            Page page = getPage("https://lectortmo.com/login");
            HtmlPage page2 = new HtmlPage(page.getWebResponse(), getCurrentWindow());
            String pageAsXml = page2.asXml();
            System.out.println(pageAsXml);
            HTMLParser htmlParser = new HtmlUnitNekoHtmlParser();
            htmlParser.parse(page.getWebResponse(), page2, true, false);

            if (isBanned(page)) {
                return;
            }

            HtmlForm form = ((HtmlPage) page).getForms().get(1);

            form.getInputByName("email").setValue("lmdjamel7867@artwerks.com");
            form.getInputByName("password").setValue("David.399385464");
            form.getInputByName("remember").setChecked(true);  // check the remember me checkbox

            DomNodeList<HtmlElement> button1 = form.getElementsByTagName("button");

            page = button1.get(0).click();  // click the button
        } catch (FailingHttpStatusCodeException e) {
            log.error("Error: ", e);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    public void logout() {
        try {
            HtmlPage page = getPage("https://lectortmo.com/logout");
            close();
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}
