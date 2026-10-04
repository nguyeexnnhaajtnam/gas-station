package vn.gasstation.dashboard.application;

import org.junit.jupiter.api.Test;
import vn.gasstation.dashboard.domain.PriceSnapshot;
import vn.gasstation.dashboard.domain.StoreInfo;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.pump.application.OnlineAggregator;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;
import java.time.OffsetDateTime;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CompositeDashboardProviderTest {
    @Test
    void returnsPartialSummaryWhenSourcesAreUnavailable() {
        var stores = mock(StoreInfoProvider.class);
        var reports = mock(ReportProvider.class);
        var tanks = mock(TankProvider.class);
        var prices = mock(PriceProvider.class);
        var online = mock(OnlineAggregator.class);
        var provider = new CompositeDashboardProvider(stores, reports, tanks, prices, online);

        when(stores.current()).thenReturn(Optional.of(new StoreInfo(true)));
        when(reports.summary(null, null)).thenReturn(Optional.empty());
        when(tanks.findAll()).thenThrow(new LegacySystemUnavailableException("parser pending"));
        when(prices.current()).thenReturn(Optional.of(new PriceSnapshot(true)));
        when(online.currentSnapshot()).thenReturn(PumpRealtimeSnapshot.unavailable(OffsetDateTime.now()));

        var summary = provider.summary(null, null);

        assertThat(summary.stationContextReady()).isTrue();
        assertThat(summary.priceSourceReady()).isTrue();
        assertThat(summary.revenue()).isNull();
        assertThat(summary.tankCount()).isNull();
        assertThat(summary.partial()).isTrue();
        assertThat(summary.onlinePumpCount()).isNull();
        assertThat(summary.pumpOverview()).isEmpty();
        assertThat(summary.unavailableSources()).containsExactly("REPORT", "TANKS", "ONLINE");
    }
}
