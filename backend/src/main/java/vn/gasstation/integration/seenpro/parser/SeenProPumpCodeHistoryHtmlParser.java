package vn.gasstation.integration.seenpro.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProPumpCodeHistoryRow;

import java.util.ArrayList;
import java.util.List;

/** Parses SeenPro HTML into internal rows for the Pump Code History business module. */
@Component
public class SeenProPumpCodeHistoryHtmlParser {
    private static final Logger log = LoggerFactory.getLogger(SeenProPumpCodeHistoryHtmlParser.class);
    private static final String ROOT_SELECTOR = ".boxMaBom";
    private static final String ROW_SELECTOR = ".rowx.color4";

    public List<SeenProPumpCodeHistoryRow> parse(String html) {
        String rawHtml = html == null ? "" : html;
        log.info("[SEENPRO] event=parser.input parser=pump-code-history rawHtmlLength={} rawHtmlPreview={}",
            rawHtml.length(), preview(rawHtml));
        var document = Jsoup.parse(rawHtml);
        String parsedHtml = document.outerHtml();
        log.info("[SEENPRO] event=parser.output parser=pump-code-history parsedHtmlPreview={}", preview(parsedHtml));

        Element body = document.body();
        log.info("[SEENPRO] event=parser.document parser=pump-code-history title={} bodyPresent={} bodyChildren={} tableCount={} theadCount={} tbodyCount={} trCount={} tdCount={}",
            document.title(), body != null, body == null ? 0 : body.childrenSize(),
            document.select("table").size(), document.select("thead").size(),
            document.select("tbody").size(), document.select("tr").size(), document.select("td").size());

        Element root = document.selectFirst(ROOT_SELECTOR);
        log.info("[SEENPRO] event=parser.root parser=pump-code-history rootSelector={} rootFound={} rootTag={} rootChildren={} rowCountInsideRoot={}",
            ROOT_SELECTOR, root != null, root == null ? "-" : root.tagName(),
            root == null ? 0 : root.childrenSize(), root == null ? 0 : root.select(ROW_SELECTOR).size());
        if (root == null) {
            failure("ROOT_NOT_FOUND", 0);
            return List.of();
        }

        var rowElements = root.select(ROW_SELECTOR);
        if (rowElements.isEmpty()) {
            failure("NO_DATA_ROWS", 0);
            return List.of();
        }

        var rows = new ArrayList<SeenProPumpCodeHistoryRow>();
        for (int rowIndex = 0; rowIndex < rowElements.size(); rowIndex++) {
            Element row = rowElements.get(rowIndex);
            try {
                rows.add(new SeenProPumpCodeHistoryRow(
                    text(row, ".maBom"), text(row, ".tenCotBom"), text(row, ".nhienLieu"),
                    text(row, ".donGia"), text(row, ".soLit"), text(row, ".thanhTien"),
                    text(row, ".thoiGianKetThucBom"), text(row, ".khachHang"),
                    text(row, ".trangThaiHD"), text(row, ".eHD")));
            } catch (RuntimeException error) {
                log.error("[ERROR] event=parser.failed provider=seenpro parser=pump-code-history failureStage=ROW_PARSE_ERROR rowIndex={} rootCause={}",
                    rowIndex, error.getClass().getSimpleName());
                throw error;
            }
        }

        log.info("[SEENPRO] event=parser.completed parser=pump-code-history selectedRoot={} rowCount={}",
            ROOT_SELECTOR, rows.size());
        return List.copyOf(rows);
    }

    private String text(Element row, String selector) {
        Element cell = row.selectFirst(selector);
        return cell == null ? null : cell.text().trim();
    }

    private String preview(String value) {
        return value.substring(0, Math.min(500, value.length()));
    }

    private void failure(String stage, int rowCount) {
        log.warn("[SEENPRO] event=parser.failed parser=pump-code-history failureStage={} rootSelector={} rowSelector={} rowCount={}",
            stage, ROOT_SELECTOR, ROW_SELECTOR, rowCount);
    }
}
