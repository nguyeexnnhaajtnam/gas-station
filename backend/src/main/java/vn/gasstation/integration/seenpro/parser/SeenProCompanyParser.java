package vn.gasstation.integration.seenpro.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProCompanyModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SeenProCompanyParser {
    private static final Logger log = LoggerFactory.getLogger(SeenProCompanyParser.class);
    private static final Pattern NAVIGATION = Pattern.compile("(?:location(?:\\.href)?\\s*=\\s*['\"])?([^'\"]*view\\.php\\?[^'\"]*gl=3[^'\"]*)", Pattern.CASE_INSENSITIVE);

    public List<SeenProCompanyModel> parse(String html) {
        var document = Jsoup.parse(html == null ? "" : html);
        var rows = document.select(".danh-sach .rowx.hover1");
        var result = new ArrayList<SeenProCompanyModel>();
        int skipped = 0;
        for (var row : rows) {
            String account = text(row, ".taiKhoan");
            String name = text(row, ".hoTen");
            if (account == null || name == null) { skipped++; continue; }
            // .matKhau is intentionally never selected or read.
            result.add(new SeenProCompanyModel(account, name, text(row, ".dienThoai"),
                normalizedEmail(text(row, ".email")), navigation(row)));
        }
        log.info("seenpro.company.parse completed rows={} skipped={}", result.size(), skipped);
        return List.copyOf(result);
    }

    private static String text(Element row, String selector) {
        Element element = row.selectFirst(selector);
        if (element == null) return null;
        String value = element.text().replaceAll("\\s+", " ").trim();
        return value.isBlank() ? null : value;
    }

    private static String normalizedEmail(String value) {
        if (value == null) return null;
        String normalized = value.toLowerCase(Locale.forLanguageTag("vi"));
        return normalized.equals("chưa cập nhật") ? null : value;
    }

    private static String navigation(Element row) {
        for (var link : row.select("a[href]")) {
            String href = link.attr("href").trim();
            if (href.contains("view.php") && href.contains("gl=3")) return href;
        }
        for (var element : row.select("[onclick]")) {
            var matcher = NAVIGATION.matcher(element.attr("onclick"));
            if (matcher.find()) return matcher.group(1).trim();
        }
        return null;
    }
}

