package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.model.SeenProTransactionModel;
import vn.gasstation.transaction.domain.InvoiceState;
import vn.gasstation.transaction.domain.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class SeenProTransactionMapper {
    public Transaction map(SeenProTransactionModel source) {
        return new Transaction(normalize(source.legacyId()), null, normalize(source.pumpCode()), null,
            normalize(source.fuelLabel()), decimal(source.unitPriceText()), decimal(source.litersText()),
            decimal(source.amountText()), "VND", dateTime(source.completedAtText()), null,
            normalize(source.customerText()), invoiceState(source.invoiceStateText()), normalize(source.invoiceNumberText()));
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
        // TODO: confirm timezone and exact format from a sanitized fixture.
        return OffsetDateTime.parse(clean, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    private InvoiceState invoiceState(String value) {
        String clean = normalize(value);
        if (clean == null) return InvoiceState.UNKNOWN;
        String lower = clean.toLowerCase(Locale.forLanguageTag("vi"));
        if (lower.equals("đã xuất") || lower.equals("đã phát hành")) return InvoiceState.ISSUED;
        if (lower.equals("chưa xuất") || lower.equals("chưa phát hành")) return InvoiceState.NOT_ISSUED;
        return InvoiceState.UNKNOWN;
    }
}
