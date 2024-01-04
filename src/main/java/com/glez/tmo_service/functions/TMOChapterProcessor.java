package com.glez.tmo_service.functions;

import lombok.extern.slf4j.Slf4j;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.html.HtmlPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TMOChapterProcessor extends TMOBookInformation {
    public TMOChapterProcessor() {
        super();
    }

    public List<File> getChapterInfo(String url, String imagePath) {
        List<File> refs = new ArrayList<>();
        List<String> images = new ArrayList<>();
        try {

            HtmlPage page = getCascadePage(url).orElseThrow(() -> new RuntimeException("Error: Cascade page not found"));

            page.getElementById("main-container").getElementsByTagName("img").forEach(img ->
            {
                String imgPath = img.getAttribute("data-src");
                images.add(imgPath);
            });

            images.forEach(imgPath -> {
                try {
                    File imagefile = new File(imagePath + imgPath.substring(imgPath.lastIndexOf("/") + 1));
                    addRequestHeader("referer", url);
                    var image = getPage(imgPath);


                    var is = image.getWebResponse().getContentAsStream();
                    FileOutputStream outputStream = new FileOutputStream(imagefile);

                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = is.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    is.close();
                    outputStream.flush();
                    outputStream.close();

                    refs.add(preprocessImage(imagefile));
                } catch (IOException e) {
                    log.error("Error when downloading image: ", e);
                }
            });
        } catch (Exception e) {
            log.error("Error when downloading chapter: ", e);
        }

        removeRequestHeader("referer");
        return refs;
    }

    private Optional<HtmlPage> getCascadePage(String url) throws IOException, FailingHttpStatusCodeException {
        HtmlPage page = getPage(url);

        if (page.getWebResponse().getStatusCode() == 404) {
            throw new FailingHttpStatusCodeException(page.getWebResponse().getStatusMessage(),page.getWebResponse());
        }

        if (isBanned(page)) {
            return Optional.empty();
        }
        if (!page.getUrl().toString().endsWith("/cascade"))
            if (page.getUrl().toString().endsWith("/paginated"))
                page = getPage(page.getUrl().toString().replace("/paginated", "/cascade"));
            else if (page.getUrl().toString().endsWith("/"))
                page = getPage(page.getUrl().toString() + "cascade");
            else
                page = getPage(page.getUrl().toString() + "/cascade");

        return Optional.of(page);
    }

    private File preprocessImage(File image) throws IOException {
        String filePath = image.getAbsolutePath().substring(0, image.getAbsolutePath().lastIndexOf("."));
        Process exec;

        exec = Runtime.getRuntime().exec("convert " + image.getAbsolutePath() + " -background white -alpha remove " +
                filePath + ".jpg");
        logProcessOutputs(exec);
        if (!image.getAbsolutePath().endsWith(".jpg")) {
            image.delete();
            return new File(filePath + ".jpg");
        }
        return image;
    }

    private void logProcessOutputs(Process exec) {
        BufferedReader output = new BufferedReader(new java.io.InputStreamReader(exec.getInputStream()));
        BufferedReader error = new BufferedReader(new java.io.InputStreamReader(exec.getErrorStream()));

        if (output.lines().findAny().isPresent() || error.lines().findAny().isPresent()) {
            log.info("Output:");
            output.lines().forEach(log::info);
            log.info("Error:");
            error.lines().forEach(log::info);
        }
    }
}
