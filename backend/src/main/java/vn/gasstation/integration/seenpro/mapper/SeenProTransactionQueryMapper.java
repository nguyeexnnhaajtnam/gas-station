package vn.gasstation.integration.seenpro.mapper;

import org.springframework.stereotype.Component;
import vn.gasstation.pumpcode.application.PumpCodeHistorySearchRequest;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SeenProTransactionQueryMapper {
    private static final DateTimeFormatter LEGACY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public Map<String, String> map(PumpCodeHistorySearchRequest query) {
        var result = new LinkedHashMap<String, String>();
        if (query.from() != null) result.put("t1", LEGACY_DATE.format(query.from()));
        if (query.to() != null) result.put("t2", LEGACY_DATE.format(query.to()));
        if (query.customer() != null) result.put("kh", query.customer());
        if (query.pumpId() != null) result.put("cb", query.pumpId());
        if (query.fuelType() != null) result.put("nl", query.fuelType());
        if (query.amountFilter() != null) result.put("tien", query.amountFilter());
        if (query.volumeFilter() != null) result.put("lit", query.volumeFilter());
        if (query.status() != null) result.put("trangthai", query.status());
        if (query.sort() != null) result.put("sapxep", query.sort());
        result.put("start", Integer.toString(Math.multiplyExact(query.page(), query.size())));
        return Map.copyOf(result);
    }

}

