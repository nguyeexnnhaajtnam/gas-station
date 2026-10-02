package vn.gasstation.integration.seenpro.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProStationModel;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeenProStationParser {
    private static final Logger log = LoggerFactory.getLogger(SeenProStationParser.class);

    public List<SeenProStationModel> parse(String html) {
        var stations = new ArrayList<SeenProStationModel>();
        int skipped = 0;
        for (Element row : Jsoup.parse(html).select(".danh-sach .rowx.hover1")) {
            String account = text(row, ".taiKhoan");
            String name = text(row, ".hoTen");
            if (account == null || name == null) {
                skipped++;
                continue;
            }
            Element view = row.selectFirst("a[href*='view.php'][href*='gl=2']");
            Element manage = row.selectFirst("a[href*='daily-quanly.php'][href*='gl=2']");
            stations.add(new SeenProStationModel(account, name, text(row, ".dienThoai"),
                email(row), href(view), href(manage)));
        }
        log.debug("[SEENPRO] event=parser.result provider=seenpro parser=station count={} skipped={}", stations.size(), skipped);
        return List.copyOf(stations);
    }

    private static String email(Element row) {
        String value = text(row, ".email");
        return value != null && value.equalsIgnoreCase("chưa cập nhật") ? null : value;
    }

    private static String text(Element row, String selector) {
        Element element = row.selectFirst(selector);
        if (element == null) return null;
        String value = element.text().trim().replaceAll("\\s+", " ");
        return value.isBlank() ? null : value;
    }

    private static String href(Element link) {
        if (link == null) return null;
        String value = link.attr("href").trim();
        return value.isBlank() ? null : value;
    }
}
