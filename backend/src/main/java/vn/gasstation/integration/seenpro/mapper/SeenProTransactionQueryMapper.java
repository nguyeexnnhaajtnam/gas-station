package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.transaction.application.TransactionQuery;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SeenProTransactionQueryMapper {
    private static final DateTimeFormatter LEGACY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public Map<String, String> map(TransactionQuery query) {
        var result = new LinkedHashMap<String, String>();
        if (query.from() != null) result.put("t1", LEGACY_DATE.format(query.from()));
        if (query.to() != null) result.put("t2", LEGACY_DATE.format(query.to()));
        if (query.customerId() != null) result.put("kh", query.customerId());
        if (query.pumpId() != null) result.put("cb", query.pumpId());
        if (query.fuelId() != null) result.put("nl", query.fuelId());
        // SeenPro offsets/page size are not confirmed. Only the observed first page is safe.
        if (query.page() != 0) throw new IllegalArgumentException("Legacy pagination contract has not been confirmed");
        result.put("start", "0");
        return Map.copyOf(result);
    }
}

