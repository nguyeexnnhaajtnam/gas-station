package vn.gasstation.dashboard.domain;

import java.math.BigDecimal;
import java.util.List;
import vn.gasstation.pump.domain.PumpRealtime;

public record DashboardSummary(
    BigDecimal revenue,
    BigDecimal litersSold,
    Long transactionCount,
    Integer tankCount,
    Integer onlinePumpCount,
    List<PumpRealtime> pumpOverview,
    boolean stationContextReady,
    boolean priceSourceReady,
    boolean partial,
    List<String> unavailableSources
) {
    public DashboardSummary {
        pumpOverview = pumpOverview == null ? List.of() : List.copyOf(pumpOverview);
        unavailableSources = unavailableSources == null ? List.of() : List.copyOf(unavailableSources);
    }
}
