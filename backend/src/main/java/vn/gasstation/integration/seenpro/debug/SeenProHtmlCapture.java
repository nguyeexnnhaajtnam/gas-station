package vn.gasstation.integration.seenpro.debug;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

@Component
public class SeenProHtmlCapture {
    private static final Logger log = LoggerFactory.getLogger(SeenProHtmlCapture.class);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Path CAPTURE_ROOT = Path.of("logs", "seenpro");
    private final SeenProDebugProperties properties;
    private final boolean devProfile;

    public SeenProHtmlCapture(SeenProDebugProperties properties, Environment environment) {
        this.properties = properties;
        this.devProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev")
            || environment.getActiveProfiles().length == 0
                && Arrays.asList(environment.getDefaultProfiles()).contains("dev");
    }

    public void capture(String relativePath, java.net.http.HttpHeaders headers, String body) {
        if (!devProfile || !properties.captureHtml() || !isHtml(headers, body)) return;
        String page = pageName(relativePath);
        String stamp = FILE_TIME.format(LocalDateTime.now());
        String stem = stamp + "_" + page;
        try {
            Files.createDirectories(CAPTURE_ROOT);
            Path htmlFile = uniqueFile(CAPTURE_ROOT, stem, ".html");
            Files.writeString(htmlFile, body, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            captureTables(body, htmlFile.getFileName().toString().replaceFirst("\\.html$", ""));
            log.info("[SEENPRO] event=html.captured page={} file={} size={}",
                page, portable(htmlFile), body.getBytes(StandardCharsets.UTF_8).length);
        } catch (IOException error) {
            log.error("[ERROR] event=html.capture.failed provider=seenpro stage=html-capture errorCode=HTML_CAPTURE_FAILED rootCause={} page={}",
                error.getClass().getSimpleName(), page);
        }
    }

    private void captureTables(String html, String captureName) throws IOException {
        var tables = Jsoup.parse(html).select("table");
        if (tables.isEmpty()) return;
        Path directory = CAPTURE_ROOT.resolve("extracted").resolve(captureName);
        Files.createDirectories(directory);
        for (int index = 0; index < tables.size(); index++) {
            Files.writeString(directory.resolve("table-" + index + ".html"), tables.get(index).outerHtml(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }

    private boolean isHtml(java.net.http.HttpHeaders headers, String body) {
        if (body == null || body.isBlank()) return false;
        String contentType = headers.firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        String sample = body.substring(0, Math.min(body.length(), 2048)).toLowerCase(Locale.ROOT);
        boolean structured = sample.contains("<!doctype html") || sample.contains("<html")
            || sample.contains("<body") || sample.contains("<table");
        return structured || contentType.contains("text/html") && sample.contains("<");
    }

    private Path uniqueFile(Path directory, String stem, String extension) throws IOException {
        Path candidate = directory.resolve(stem + extension);
        int suffix = 1;
        while (Files.exists(candidate)) candidate = directory.resolve(stem + "_" + suffix++ + extension);
        return candidate;
    }

    private String pageName(String relativePath) {
        String path = relativePath == null ? "unknown" : relativePath.split("[?#]", 2)[0];
        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(slash + 1);
        path = path.replaceFirst("(?i)\\.php$", "").replaceAll("[^A-Za-z0-9_-]", "_");
        return path.isBlank() ? "unknown" : path;
    }

    private String portable(Path path) { return path.toString().replace('\\', '/'); }
}
