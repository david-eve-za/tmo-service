package com.glez.tmo_service.functions;

import com.glez.common.entities.mng.Book;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TMOProfileBooks extends TMOProfile {
    public TMOProfileBooks() {
        super();
    }

    public List<Book> getBooks(String uri) {
        List<Book> books = new ArrayList<>();
        try {
            HtmlPage page = getPage(uri);
            if (isBanned(page)) {
                return books;
            }
            List<HtmlDivision> byXPath = page.getByXPath("//*[@id=\"app\"]/section/main/div/div[2]/div[1]/div/div");
            byXPath.remove(0);

            for (HtmlDivision div : byXPath) {
                Book book = new Book();
                book.setUrl(div.getElementsByTagName("a").get(0).getAttribute("href").trim());
                book.setTitle(div.getElementsByTagName("h4").get(0).getTextContent().trim());
                books.add(book);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return books;
    }
}
