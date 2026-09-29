package vn.gasstation.dashboard.application;

import org.junit.jupiter.api.Test;
import vn.gasstation.dashboard.domain.PriceSnapshot;
import vn.gasstation.dashboard.domain.StoreInfo;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.tank.application.TankProvider;
import vn.gasstation.transaction.application.TransactionProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CompositeDashboardProviderTest {
    @Test
    void returnsPartialSummaryWithoutCallingUnfinishedTransactionProvider() {
        var stores = mock(StoreInfoProvider.class);
        var reports = mock(ReportProvider.class);
        var tanks = mock(TankProvider.class);
        var prices = mock(PriceProvider.class);
        var transactions = mock(TransactionProvider.class);
        var provider = new CompositeDashboardProvider(stores, reports, tanks, prices, transactions);

        when(stores.current()).thenReturn(Optional.of(new StoreInfo(true)));
        when(reports.summary(null, null)).thenReturn(Optional.empty());
        when(tanks.findAll()).thenThrow(new LegacySystemUnavailableException("parser pending"));
        when(prices.current()).thenReturn(Optional.of(new PriceSnapshot(true)));

        var summary = provider.summary(null, null);

        assertThat(summary.stationContextReady()).isTrue();
        assertThat(summary.priceSourceReady()).isTrue();
        assertThat(summary.revenue()).isNull();
        assertThat(summary.tankCount()).isNull();
        assertThat(summary.partial()).isTrue();
        assertThat(summary.unavailableSources()).containsExactly("REPORT", "TANKS", "TRANSACTIONS");
        verifyNoInteractions(transactions);
    }
}
