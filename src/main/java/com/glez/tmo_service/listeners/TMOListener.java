package com.glez.tmo_service.listeners;

import com.glez.common.entities.mng.Book;
import com.glez.common.entities.mng.Chapter;
import com.glez.common.utils.JsonUtils;
import com.glez.tmo_service.utils.TMOFans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class TMOListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    //    private TMOFans tmoFans;
    private String bookPath;

    @KafkaListener(topics = "tmo-scan-library-topic", concurrency = "1")
    public void handleScanListener(String message) {
        try (TMOFans tmoFans = new TMOFans()) {
            log.info("Received Messasge in topic tmo-scan-library-topic: {}", message);
            var profileList = tmoFans.getProfileList();
            profileList.forEach(profile -> tmoFans.getBooks(profile).forEach(book -> this.kafkaTemplate.send("save-book-topic", JsonUtils.toJson(book))));
        } catch (Exception e) {
            log.error("Error while scanning library: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "tmo-update-book", concurrency = "1")
    public void handleUpdateBook(String message) {
        try (TMOFans tmoFans = new TMOFans()) {
            log.info("Received Messasge in topic tmo-update-book: {}", message);
            Book book = JsonUtils.fromJson(message, Book.class);

            assert book != null;
            setBookPath(book.getTitle());

            var bookInfo = tmoFans.getBookInfo(book.getUrl());
            var chapters = new ArrayList<Chapter>();

            bookInfo.forEach(chapterInfo -> {
                Chapter chapter = new Chapter();
                chapter.setUrl(chapterInfo.get("url"));
                chapter.setTitle(correct(chapterInfo.get("title")));
                chapter.setIsRead(false);
                chapters.add(0, chapter);
            });

            log.info("Found {} chapters for book: {}", chapters.size(), book.getTitle());
            chapters.forEach(chapter -> {
                if (book.getChapters().stream().noneMatch(c -> c.getTitle().equals(chapter.getTitle()))) {
                    chapter.setBookId(book.getId());
                    this.kafkaTemplate.send("save-chapter-topic", JsonUtils.toJson(chapter));
                }
                if (!pdfName(chapter.getTitle()).exists()) {
                    log.info("Adding chapter: {} on book: {}", chapter.getTitle(), book.getTitle());
                    waitSeconds();
                    var chapterInfo = tmoFans.getChapterInfo(chapter.getUrl(), this.bookPath);

                    if (chapterInfo.isEmpty()) {
                        log.error("Error while getting chapter info: {}", chapter.getUrl());
                        return;
                    }

                    File pdfFile = pdfName(chapter.getTitle());
                    PDDocument document = new PDDocument();
                    try {
                        log.info("Creating PDF for chapter: {} of book: {}", chapter.getTitle(), book.getTitle());
                        for (File image : chapterInfo) {
                            PDImageXObject pdImage = PDImageXObject.createFromFile(image.getAbsolutePath(), document);
                            PDPage page = new PDPage(new PDRectangle(pdImage.getWidth(), pdImage.getHeight()));
                            document.addPage(page);
                            PDPageContentStream contentStream = new PDPageContentStream(document, page);
                            contentStream.drawImage(pdImage, 0, 0);
                            contentStream.close();
                        }
                        document.save(pdfFile.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("Error creating PDF for chapter: {} of book: {}", chapter.getTitle(), book.getTitle(), e);
                    } finally {
                        for (File image : chapterInfo) {
                            try {
                                Files.delete(image.toPath());
                            } catch (IOException e) {
                                log.error("Error", e);
                            }
                        }
                        try {
                            document.close();
                        } catch (IOException e) {
                            log.error("Error", e);
                        }
                    }
                    log.info("Added chapter: {} with: {} pages on book: {}", chapter.getTitle(), chapter.getPages().size(), book.getTitle());
                }
            });
            waitSeconds();
        } catch (Exception e) {
            log.error("Error while updating book: {}", e.getMessage());
        }
    }

    private void waitSeconds() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            log.error("Error while sleeping", e);
        }
    }

    private File pdfName(String title) {
        File file = new File(this.bookPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(this.bookPath + title + ".pdf");
    }

    private void setBookPath(String bookTitle) {
        var hash = JsonUtils.calculateHash(bookTitle);
        this.bookPath = "/Volumes/Elements/Peliculas/.Hide/MNG/" + hash + "/";
    }

    private String correct(String title) {
        char[] chars = {'/', ':', '?', '*', '"', '<', '>', '|'};
        Matcher matcher = Pattern.compile("[" + new String(chars) + "]").matcher(title);
        return matcher.replaceAll("-");
    }
}
