package com.glez.tmo_service.base;

import lombok.extern.slf4j.Slf4j;
import org.htmlunit.BrowserVersion;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;

@Slf4j
public class BrowserBase extends WebClient {

    private static final int DEFAULT_REQUEST_TIMEOUT = (60 * 7) * 1000;

    public BrowserBase() {
//        super(BrowserVersion.FIREFOX);
        super(BrowserVersion.CHROME);
//        super(BrowserVersion.INTERNET_EXPLORER);
//        super(BrowserVersion.EDGE);
//        super(BrowserVersion.FIREFOX_ESR);
        setup();

    }

    private void setup() {

        getCache().clear();
        getCookieManager().clearCookies();
        getOptions().setJavaScriptEnabled(true); // Enable JS execution in browser (needed for some pages)
        getOptions().setCssEnabled(false);
        getOptions().setThrowExceptionOnScriptError(false);
        getOptions().setThrowExceptionOnFailingStatusCode(false);
        getOptions().setPrintContentOnFailingStatusCode(false);
        getOptions().setUseInsecureSSL(true);
        getOptions().setRedirectEnabled(true); // Set to false to avoid infinite redirects
        getOptions().setAppletEnabled(false);
        getOptions().setGeolocationEnabled(false);
        getOptions().setPopupBlockerEnabled(true);
        getOptions().setDoNotTrackEnabled(true);
        getOptions().setDownloadImages(true);
        getOptions().setTimeout(DEFAULT_REQUEST_TIMEOUT);
        // Bypass Cloudflare
        addRequestHeader("Pragma", "no-cache");
    }

    public Boolean isBanned(Page page) {
        if (((HtmlPage) page).asXml().contains("Has sido baneado")) {
            log.error("User is banned");
            close();
            return true;
        }
        return false;
    }

    public void close() {
        getCache().clear();
        getCookieManager().clearCookies();
        super.close();
    }

}
