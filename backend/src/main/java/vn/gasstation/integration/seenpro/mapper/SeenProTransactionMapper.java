package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import vn.gasstation.pumpcode.domain.InvoiceStatus;
import vn.gasstation.pumpcode.domain.PumpCodeHistory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class SeenProTransactionMapper {
    public PumpCodeHistory map(SeenProTransactionModel source) {
        return new PumpCodeHistory(normalize(source.pumpCode()), normalize(source.dispenser()),
            normalize(source.fuelLabel()), decimal(source.unitPriceText()), decimal(source.litersText()),
            decimal(source.amountText()), dateTime(source.completedAtText()), normalize(source.customerText()),
            invoiceState(source.invoiceStateText()), normalize(source.invoiceNumberText()));
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim().replaceAll("\\s+", " "); }
    private BigDecimal decimal(String value) {
        String clean = normalize(value);
        if (clean == null) return null;
        clean = clean.replaceAll("[^0-9,.-]", "");
        int comma = clean.lastIndexOf(','), dot = clean.lastIndexOf('.');
        if (comma > dot) {
            clean = clean.replace(".", "").replace(',', '.');
        } else if (dot >= 0 && comma < 0 && clean.length() - dot - 1 == 3) {
            clean = clean.replace(".", ""); // observed Vietnamese thousands separator; fixture test protects this boundary.
        } else {
            clean = clean.replace(",", "");
        }
        return new BigDecimal(clean);
    }
    private OffsetDateTime dateTime(String value) {
        String clean = normalize(value);
        if (clean == null) return null;
        try { return OffsetDateTime.parse(clean, DateTimeFormatter.ISO_OFFSET_DATE_TIME); }
        catch (java.time.format.DateTimeParseException ignored) {
            var local = java.time.LocalDateTime.parse(clean, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]"));
            return local.atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toOffsetDateTime();
        }
    }
    private InvoiceStatus invoiceState(String value) {
        String clean = normalize(value);
        if (clean == null) return InvoiceStatus.UNKNOWN;
        String lower = clean.toLowerCase(Locale.forLanguageTag("vi"));
        if (lower.contains("đã xuất") || lower.contains("đã phát hành")) return InvoiceStatus.ISSUED;
        if (lower.contains("chưa xuất") || lower.contains("chưa phát hành")) return InvoiceStatus.NOT_ISSUED;
        if (lower.equals("đã xuất") || lower.equals("đã phát hành")) return InvoiceStatus.ISSUED;
        if (lower.equals("chưa xuất") || lower.equals("chưa phát hành")) return InvoiceStatus.NOT_ISSUED;
        return InvoiceStatus.UNKNOWN;
    }
}
