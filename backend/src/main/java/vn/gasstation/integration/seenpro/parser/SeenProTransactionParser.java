package vn.gasstation.integration.seenpro.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import java.text.Normalizer;
import java.util.*;

public class SeenProTransactionParser {
    private static final Logger log = LoggerFactory.getLogger(SeenProTransactionParser.class);
    private static final List<String> EXPECTED_HEADERS = List.of(
        "Mã bơm/Mã cột", "Trụ bơm/Cột bơm", "Nhiên liệu", "Đơn giá/Giá", "Số lít/Lít",
        "Thành tiền/Số tiền/Tiền", "Thời gian/Kết thúc/Ngày giờ", "Khách hàng", "Hóa đơn", "Trạng thái");

    public List<SeenProTransactionModel> parse(String html) {
        String rawHtml = html == null ? "" : html;
        log.info("[SEENPRO] event=parser.input parser=pump-code-history rawHtmlLength={} rawHtmlPreview={}",
            rawHtml.length(), preview(rawHtml));
        var document = Jsoup.parse(rawHtml);
        String parsedHtml = document.outerHtml();
        log.info("[SEENPRO] event=parser.output parser=pump-code-history parsedHtmlPreview={}",
            preview(parsedHtml));
        Element body = document.body();
        log.info("[SEENPRO] event=parser.document parser=pump-code-history title={} bodyPresent={} bodyChildren={} tableCount={} theadCount={} tbodyCount={} trCount={} tdCount={}",
            document.title(), body != null, body == null ? 0 : body.childrenSize(),
            document.select("table").size(), document.select("thead").size(),
            document.select("tbody").size(), document.select("tr").size(), document.select("td").size());
        log.info("[SEENPRO] event=parser.root parser=pump-code-history rootSelector=document rootFound=true rootTag={} rootChildren={} tableCountInsideRoot={}",
            document.tagName(), document.childrenSize(), document.select("table").size());
        var tables = document.select("table");
        log.info("[SEENPRO] event=parser.tables parser=pump-code-history tableCount={}", tables.size());
        if (tables.isEmpty()) {
            failure("NO_TABLE_FOUND", List.of());
            return List.of();
        }
        var actualHeaders = new ArrayList<List<String>>();
        boolean partialHeaderMatch = false;
        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            Element table = tables.get(tableIndex);
            Element header = table.selectFirst("thead tr");
            if (header == null) header = table.selectFirst("tr:has(th)");
            List<String> rawHeaders = header == null ? List.of() : header.select("th, td").eachText();
            actualHeaders.add(rawHeaders);
            log.info("[SEENPRO] event=parser.table parser=pump-code-history tableIndex={} cssClass={} id={} rows={} columns={} headers={}",
                tableIndex, table.className(), table.id(), table.select("tr").size(), columnCount(table), rawHeaders);
            if (header == null) continue;
            Map<String, Integer> columns = columns(header);
            partialHeaderMatch |= hasAnyExpectedHeader(columns);
            if (!isTransactionTable(columns)) continue;
            log.info("[SEENPRO] event=parser.table.selected parser=pump-code-history selectedTable={}", tableIndex);
            var result = new ArrayList<SeenProTransactionModel>();
            for (Element row : table.select("tbody tr, tr")) {
                if (row == header || !row.select("th").isEmpty()) continue;
                Elements cells = row.select("td");
                if (cells.isEmpty()) continue;
                try {
                    result.add(new SeenProTransactionModel(
                        cell(cells, index(columns, "ma bom", "ma cot")),
                        cell(cells, index(columns, "tru bom", "cot bom", "tru")),
                        cell(cells, index(columns, "nhien lieu", "mat hang")),
                        cell(cells, index(columns, "don gia", "gia")),
                        cell(cells, index(columns, "so lit", "lit", "san luong")),
                        cell(cells, index(columns, "thanh tien", "so tien", "tien")),
                        cell(cells, index(columns, "thoi gian", "ket thuc", "ngay gio")),
                        cell(cells, index(columns, "khach hang", "khach")),
                        cell(cells, index(columns, "trang thai hoa don", "trang thai")),
                        cell(cells, index(columns, "so hoa don", "hoa don"))));
                } catch (RuntimeException error) {
                    log.error("[ERROR] event=parser.failed provider=seenpro parser=pump-code-history failureStage=ROW_PARSE_ERROR tableIndex={} rootCause={}",
                        tableIndex, error.getClass().getSimpleName());
                    throw error;
                }
            }
            if (result.isEmpty()) failure(table.selectFirst("tbody") == null ? "NO_TBODY" : "NO_DATA_ROWS", actualHeaders);
            return List.copyOf(result);
        }
        failure(partialHeaderMatch ? "HEADER_MISMATCH" : "NO_MATCHING_HEADERS", actualHeaders);
        return List.of();
    }

    private int columnCount(Element table) {
        return table.select("tr").stream().mapToInt(row -> row.select("th, td").size()).max().orElse(0);
    }

    private String preview(String value) {
        return value.substring(0, Math.min(500, value.length()));
    }

    private boolean hasAnyExpectedHeader(Map<String, Integer> columns) {
        return index(columns, "ma bom", "ma cot", "tru bom", "nhien lieu", "don gia", "so lit",
            "thanh tien", "thoi gian", "khach hang", "hoa don", "trang thai") >= 0;
    }
    private void failure(String stage, List<List<String>> actualHeaders) {
        log.warn("[SEENPRO] event=parser.failed parser=pump-code-history failureStage={} expectedHeaders={} actualHeaders={}",
            stage, EXPECTED_HEADERS, actualHeaders);
    }

    private Map<String, Integer> columns(Element header) {
        var result = new HashMap<String, Integer>();
        var cells = header.select("th, td");
        for (int i = 0; i < cells.size(); i++) result.put(normalize(cells.get(i).text()), i);
        return result;
    }
    private boolean isTransactionTable(Map<String, Integer> columns) {
        return index(columns, "thanh tien", "so tien", "tien") >= 0
            && index(columns, "so lit", "lit", "san luong") >= 0
            && index(columns, "thoi gian", "ket thuc", "ngay gio") >= 0;
    }
    private int index(Map<String, Integer> columns, String... names) {
        for (String name : names) {
            Integer exact = columns.get(name);
            if (exact != null) return exact;
            for (var entry : columns.entrySet()) if (entry.getKey().contains(name)) return entry.getValue();
        }
        return -1;
    }
    private String cell(Elements cells, int index) {
        return index < 0 || index >= cells.size() ? null : cells.get(index).text().trim();
    }
    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
            .replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ").trim();
    }
}
