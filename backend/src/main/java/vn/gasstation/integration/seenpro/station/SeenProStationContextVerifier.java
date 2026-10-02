package vn.gasstation.integration.seenpro.station;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Verifies that a SeenPro page header still belongs to the selected station. */
@Component
public class SeenProStationContextVerifier {
    private static final String STATION_NAME_SELECTOR = String.join(", ",
        "div.tenCuaHang", ".tenCuaHang", "#tenCuaHang",
        ".ten-cuahang", "#ten-cuahang", ".tencuahang", "#tencuahang",
        ".store-name", "#store-name", "[data-field=station-name]");
    private static final Set<String> BUSINESS_PREFIX_WORDS = Set.of(
        "cong", "ty", "tnhh", "dntn", "doanh", "nghiep", "tu", "nhan",
        "cua", "hang", "tram", "xang", "dau", "xd", "tm", "thuong", "mai");

    public Verification verify(String html, String expectedStationName) {
        if (html == null || html.isBlank() || expectedStationName == null || expectedStationName.isBlank()) {
            return new Verification(false, null);
        }
        Document document = Jsoup.parse(html);
        for (Element stationName : document.select(STATION_NAME_SELECTOR)) {
            String displayedName = compact(stationName.text());
            if (namesMatch(expectedStationName, displayedName)) {
                return new Verification(true, displayedName);
            }
        }

        // Some SeenPro variants do not mark up the store name separately. The body remains
        // acceptable only when it contains the selected station's distinctive name tokens.
        Element body = document.body();
        if (body != null && namesMatch(expectedStationName, body.text())) {
            return new Verification(true, compact(body.text()));
        }
        return new Verification(false, null);
    }

    private static boolean namesMatch(String expectedName, String displayedText) {
        String expected = normalize(expectedName);
        String displayed = normalize(displayedText);
        if (expected.isBlank() || displayed.isBlank()) return false;
        if (displayed.contains(expected)) return true;

        List<String> distinctiveWords = Arrays.stream(expected.split(" "))
            .filter(word -> !word.isBlank() && !BUSINESS_PREFIX_WORDS.contains(word))
            .toList();
        if (distinctiveWords.isEmpty()) return false;

        String distinctiveName = String.join(" ", distinctiveWords);
        if (distinctiveWords.size() == 1) {
            String word = distinctiveWords.get(0);
            return word.length() >= 4 && containsWholePhrase(displayed, word);
        }
        return containsWholePhrase(displayed, distinctiveName);
    }

    private static boolean containsWholePhrase(String text, String phrase) {
        return (" " + text + " ").contains(" " + phrase + " ");
    }

    private static String normalize(String value) {
        return Normalizer.normalize(compact(value), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim();
    }

    private static String compact(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    public record Verification(boolean matches, String matchedHeaderText) {}
}
