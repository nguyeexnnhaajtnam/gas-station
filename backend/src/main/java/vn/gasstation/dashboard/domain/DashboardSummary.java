package vn.gasstation.dashboard.domain;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
    BigDecimal revenue,
    BigDecimal litersSold,
    Long transactionCount,
    Integer tankCount,
    boolean stationContextReady,
    boolean priceSourceReady,
    boolean partial,
    List<String> unavailableSources
) {
    public DashboardSummary {
        unavailableSources = unavailableSources == null ? List.of() : List.copyOf(unavailableSources);
    }
}
